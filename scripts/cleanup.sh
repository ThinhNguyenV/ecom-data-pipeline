#!/usr/bin/env bash
# =============================================================
# cleanup.sh — Xoá các file/thư mục cũ sau khi restructure
# Chạy 1 lần: bash scripts/cleanup.sh
# =============================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "=== Dọn dẹp cấu trúc cũ của ecom-data-pipeline ==="
echo ""

# ── 1. Xoá thư mục ci/ cũ (đã chuyển sang .github/workflows/) ──
if [ -d "ci" ]; then
    rm -rf ci/
    echo "✓ Đã xoá ci/  (đã có .github/workflows/ci.yml)"
fi

# ── 2. Xoá thư mục src/ con cũ ──────────────────────────────────
# File thật đã nằm ở src/ecom_pipeline/
for old_dir in src/api src/common src/ingestion src/processing; do
    if [ -d "$old_dir" ]; then
        rm -rf "$old_dir"
        echo "✓ Đã xoá $old_dir/  (đã có src/ecom_pipeline/$(basename $old_dir)/)"
    fi
done

# ── 3. Xoá requirements.txt cũ (đã có pyproject.toml) ───────────
if [ -f "requirements.txt" ]; then
    rm requirements.txt
    echo "✓ Đã xoá requirements.txt  (đã có pyproject.toml)"
fi

# ── 4. Xoá data/ rỗng ────────────────────────────────────────────
if [ -d "data" ] && [ -z "$(ls -A data)" ]; then
    rm -rf data/
    echo "✓ Đã xoá data/  (thư mục rỗng)"
fi

# ── 5. Xoá tests/__init__.py cũ (không cần với src layout) ───────
if [ -f "tests/__init__.py" ]; then
    rm tests/__init__.py
    echo "✓ Đã xoá tests/__init__.py  (không cần với pytest rootdir)"
fi

echo ""
echo "=== Dọn dẹp hoàn tất! ==="
echo ""
echo "Cấu trúc hiện tại:"
echo "  .github/workflows/   CI/CD (ci.yml + release.yml)"
echo "  src/ecom_pipeline/   Python package (src layout)"
echo "  tests/unit/          Unit tests"
echo "  tests/integration/   Integration tests (placeholder)"
