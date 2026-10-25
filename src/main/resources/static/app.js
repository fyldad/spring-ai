const METADATA_FIELDS = [
  "symbol_id",
  "parse_status",
  "language",
  "file_hash",
  "maven_parent",
  "parent_document_id",
  "namespace_uri",
  "build_system",
  "inventory_status",
  "revision_status",
  "file_kind",
  "parser_version",
  "semantic_xml",
  "chunk_index",
  "dependency_management",
  "dependency_model",
  "configuration_redacted",
  "xml_path",
  "generated",
  "profile",
  "resolution_status",
  "doc_content",
  "reader",
  "element_qname",
  "repository_id",
  "module_path",
  "module",
  "contract_context",
  "encoding",
  "active_profiles",
  "content_origin",
  "relative_path",
  "source_set",
  "maven_resolution_policy",
  "module_id",
  "root_element",
  "location_basis",
  "total_chunks",
];

const form = document.getElementById("ask-form");
const filtersEl = document.getElementById("filters");
const emptyFiltersEl = document.getElementById("empty-filters");
const addFilterBtn = document.getElementById("add-filter");
const clearBtn = document.getElementById("clear-btn");
const submitBtn = document.getElementById("submit-btn");
const overlay = document.getElementById("overlay");
const resultSection = document.getElementById("result-section");
const answerEl = document.getElementById("answer");
const errorEl = document.getElementById("error");
const rowTemplate = document.getElementById("filter-row-template");

function setLoading(loading) {
  document.body.classList.toggle("is-loading", loading);
  overlay.hidden = !loading;
  form.querySelectorAll("input, textarea, select, button").forEach((el) => {
    el.disabled = loading;
  });
  // Keep overlay from being "disabled" — it's not a form control
}

function syncFilterUi() {
  const rows = [...filtersEl.querySelectorAll(".filter-row")];
  emptyFiltersEl.hidden = rows.length > 0;
  rows.forEach((row, index) => {
    row.classList.toggle("is-first", index === 0);
  });
}

function populateFieldSelect(select) {
  METADATA_FIELDS.forEach((field) => {
    const option = document.createElement("option");
    option.value = field;
    option.textContent = field;
    select.appendChild(option);
  });
}

function addFilterRow() {
  const node = rowTemplate.content.cloneNode(true);
  const row = node.querySelector(".filter-row");
  const fieldSelect = row.querySelector(".filter-field");
  populateFieldSelect(fieldSelect);

  row.querySelector(".remove-filter").addEventListener("click", () => {
    row.remove();
    syncFilterUi();
  });

  filtersEl.appendChild(row);
  syncFilterUi();
  fieldSelect.focus();
}

function collectFilters() {
  return [...filtersEl.querySelectorAll(".filter-row")]
    .map((row) => {
      const field = row.querySelector(".filter-field").value;
      const value = row.querySelector(".filter-value").value.trim();
      const type = row.querySelector(".filter-type").value;
      return { field, value, type };
    })
    .filter((f) => f.field && f.value);
}

function showAnswer(text) {
  resultSection.hidden = false;
  errorEl.hidden = true;
  errorEl.textContent = "";
  answerEl.textContent = text;
}

function showError(message) {
  resultSection.hidden = false;
  answerEl.textContent = "";
  errorEl.hidden = false;
  errorEl.textContent = message;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const question = document.getElementById("question").value.trim();
  if (!question) {
    document.getElementById("question").focus();
    return;
  }

  const incomplete = [...filtersEl.querySelectorAll(".filter-row")].some((row) => {
    const field = row.querySelector(".filter-field").value;
    const value = row.querySelector(".filter-value").value.trim();
    return !field || !value;
  });
  if (incomplete) {
    showError("Each filter needs a field and a value.");
    return;
  }

  const payload = {
    question,
    metaDataFilter: collectFilters(),
  };

  setLoading(true);
  try {
    const response = await fetch("/ask/company", {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json, text/plain, */*" },
      body: JSON.stringify(payload),
    });

    const raw = await response.text();
    if (!response.ok) {
      let message = `Request failed (${response.status})`;
      try {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed.validationErrors) && parsed.validationErrors.length) {
          message = parsed.validationErrors.join("; ");
        } else {
          message = parsed.detail || parsed.message || parsed.error || raw || message;
        }
      } catch {
        if (raw) message = raw;
      }
      showError(message);
      return;
    }

    let answer = raw;
    try {
      const parsed = JSON.parse(raw);
      if (typeof parsed === "string") {
        answer = parsed;
      } else if (parsed && typeof parsed.answer === "string") {
        answer = parsed.answer;
      }
    } catch {
      // plain text body
    }
    showAnswer(answer);
  } catch (err) {
    showError(err.message || "Network error");
  } finally {
    setLoading(false);
  }
});

addFilterBtn.addEventListener("click", addFilterRow);

clearBtn.addEventListener("click", () => {
  form.reset();
  filtersEl.innerHTML = "";
  syncFilterUi();
  resultSection.hidden = true;
  answerEl.textContent = "";
  errorEl.hidden = true;
  document.getElementById("question").focus();
});

// Block pointer events under overlay (extra safety while loading)
overlay.addEventListener("click", (e) => e.stopPropagation());
overlay.addEventListener("keydown", (e) => e.preventDefault());

syncFilterUi();
