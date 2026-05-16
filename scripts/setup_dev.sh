#!/usr/bin/env bash
# =============================================================
# setup_dev.sh — First-time development environment setup
# Usage: bash scripts/setup_dev.sh
# =============================================================
set -euo pipefail

BLUE='\033[0;34m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC} $*"; }
success() { echo -e "${GREEN}[OK]${NC}   $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $*"; }

info "Setting up ecom-data-pipeline development environment..."

# 1. Python package (editable install)
info "Installing Python package in editable mode..."
pip install -e ".[dev,quality]" --quiet
success "Python deps installed"

# 2. Pre-commit hooks
info "Installing pre-commit hooks..."
pre-commit install
pre-commit install --hook-type commit-msg
success "Pre-commit hooks installed"

# 3. Copy env template
if [ ! -f .env.local ]; then
    cp .env .env.local
    warn ".env.local created — fill in real values before running services"
else
    info ".env.local already exists, skipping"
fi

# 4. Docker services
info "Starting infrastructure services..."
cd deploy/docker
docker compose up -d postgres elasticsearch kafka zookeeper
cd ../..
success "Core services started"

# 5. Wait for PostgreSQL
info "Waiting for PostgreSQL to be ready..."
until docker exec postgres pg_isready -U user -d ecom_dw > /dev/null 2>&1; do
    sleep 2
done
success "PostgreSQL ready"

# 6. dbt setup
info "Installing dbt packages..."
cd dbt && dbt deps --quiet && cd ..
success "dbt packages installed"

echo ""
success "Development environment ready!"
echo ""
echo "  Next steps:"
echo "    make docker-up       # Start all services"
echo "    make kafka-producer  # Start sending events"
echo "    make dbt-run         # Transform data"
echo "    make backend-run     # Start REST API"
echo "    open http://localhost:8090/swagger-ui.html"
