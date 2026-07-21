# Local AI resume tailoring

## Lifecycle

1. Configure an immutable master DOCX.
2. Open an ingested job and generate a tailored version.
3. CareerOS extracts source bullets, renders prompt v1.0, and requests
   structured JSON from local Ollama.
4. Grounding validation rejects technologies and metrics absent from the
   master. One retry is allowed; the safe fallback retains original bullets.
5. A copied DOCX is stored under
   `<company>/<date>/<job-id>/v####/<name>.docx` so every generation is
   readable and prior versions cannot be overwritten. PDF generation is
   currently disabled.
6. Review validation warnings, download artifacts, compare versions, and
   optionally link generation to an application.

## Troubleshooting

- **Ollama unavailable:** run `curl http://localhost:11434/api/tags` and pull
  the configured model. For Docker, ensure Ollama accepts the
  `host.docker.internal` connection.
- **Master missing:** use an absolute `MASTER_RESUME_PATH`, or place the file at
  `data/master-resume.docx` with Compose.
- **Externally managed Python / python-docx missing:** create a local environment
  using the Python selected by pyenv:

  ```bash
  python3 -m venv .venv
  source .venv/bin/activate
  python -m pip install --upgrade pip
  python -m pip install python-docx
  export PYTHON_PATH="$PWD/.venv/bin/python"
  ```

  Do not use `--break-system-packages`; CareerOS can invoke the virtual
  environment directly through `PYTHON_PATH`.
- **No bullets found:** ensure bullet paragraphs use a Word list style.
- **Validation fallback:** inspect warnings in Resume Library. No ungrounded
  or blank generated bullets are written when validation still fails after retry.

## Privacy

CareerOS implements no cloud AI adapter in this milestone. Resume text is sent
only to `OLLAMA_BASE_URL`, which defaults to localhost. Document processing and
artifact storage are local. Operators should keep the output directory outside
publicly served paths and apply normal filesystem permissions and backups.

## Known limitations

- Paragraph-list bullets are replaced in document order; complex nested table
  templates may require a future template-specific adapter.
- Grounding validation currently has explicit technology and numeric-metric
  checks. Employer, certification, and responsibility checks rely on the
  constrained prompt and conservative fallback and can be expanded into a
  richer deterministic fact inventory.
- Generation is synchronous; a durable background queue is recommended for
  multi-user deployments.
