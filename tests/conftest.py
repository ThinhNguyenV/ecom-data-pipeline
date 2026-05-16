"""
conftest.py — Shared pytest fixtures for all tests.
Place fixtures here that are used across unit and integration tests.
"""
import os
import pytest
from unittest.mock import MagicMock


# ── Environment setup ─────────────────────────────────────────────
@pytest.fixture(autouse=True)
def set_test_env(monkeypatch):
    """Ensure tests never touch real external services."""
    monkeypatch.setenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9093")
    monkeypatch.setenv("POSTGRES_HOST", "localhost")
    monkeypatch.setenv("ES_NODES", "localhost")
    monkeypatch.setenv("ES_PORT", "9200")


# ── Shared config fixture ─────────────────────────────────────────
@pytest.fixture
def sample_config():
    """Minimal valid config dict for unit tests."""
    return {
        "kafka": {
            "bootstrap_servers": "localhost:9093",
            "topics": {
                "user_behavior": "user_behavior",
                "products": "products",
            },
            "consumer_group": "test_group",
        },
        "elasticsearch": {
            "nodes": "localhost",
            "port": 9200,
            "index_products": "products",
        },
        "model": {
            "name": "all-MiniLM-L6-v2",
            "vector_dims": 384,
        },
        "spark": {
            "app_name": "TestApp",
            "master": "local[*]",
            "checkpoint_dir": "/tmp/checkpoints",
            "output_dir": "/tmp/raw",
            "jars": [],
        },
    }


# ── Sample product fixtures ───────────────────────────────────────
@pytest.fixture
def sample_product():
    return {
        "product_id": "P001",
        "name": "Giày chạy bộ Nike Air Max",
        "category": "Footwear",
        "description": "Giày thể thao chạy bộ bền bỉ",
    }


@pytest.fixture
def sample_behavior():
    return {
        "user_id": "U1234",
        "product_id": "P001",
        "action": "purchase",
        "timestamp": "2025-01-10 10:00:00",
    }
