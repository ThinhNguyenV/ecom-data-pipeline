"""
Unit tests for EcomSearchService.
Run: pytest tests/unit/test_search_service.py -v
"""
import pytest
from unittest.mock import MagicMock, patch

from ecom_pipeline.api.search_service import EcomSearchService


@pytest.fixture
def mock_es():
    with patch("ecom_pipeline.api.search_service.Elasticsearch") as MockEs:
        instance = MagicMock()
        MockEs.return_value = instance
        yield instance


@pytest.fixture
def mock_model():
    with patch("ecom_pipeline.api.search_service.SentenceTransformer") as MockModel:
        instance = MagicMock()
        instance.encode.return_value = [0.1] * 384
        MockModel.return_value = instance
        yield instance


@pytest.fixture
def search_service(mock_es, mock_model):
    return EcomSearchService(
        es_host="localhost",
        es_port=9200,
        index_name="products",
        model_name="all-MiniLM-L6-v2",
    )


class TestEcomSearchService:

    def test_init_connects_to_elasticsearch(self, mock_es, mock_model):
        svc = EcomSearchService("localhost", 9200, "products", "all-MiniLM-L6-v2")
        assert svc.es is not None
        assert svc.model is not None

    def test_create_index_creates_when_not_exists(self, search_service, mock_es):
        mock_es.indices.exists.return_value = False
        search_service.create_index_with_knn_mapping()
        mock_es.indices.create.assert_called_once()

    def test_create_index_skips_when_already_exists(self, search_service, mock_es):
        mock_es.indices.exists.return_value = True
        search_service.create_index_with_knn_mapping()
        mock_es.indices.create.assert_not_called()

    def test_semantic_search_returns_results(self, search_service, mock_es, mock_model):
        mock_es.search.return_value = {
            "hits": {
                "hits": [
                    {
                        "_score": 0.95,
                        "_source": {
                            "product_id": "P001",
                            "name": "Giày Nike Air Max",
                            "description": "Giày thể thao bền bỉ",
                        },
                    }
                ]
            }
        }
        results = search_service.semantic_search("giày chạy bộ", top_k=3)
        assert len(results) == 1
        assert results[0]["product_id"] == "P001"
        assert results[0]["score"] == 0.95

    def test_semantic_search_encodes_query(self, search_service, mock_es, mock_model):
        mock_es.search.return_value = {"hits": {"hits": []}}
        search_service.semantic_search("tai nghe chống ồn")
        mock_model.encode.assert_called_once_with("tai nghe chống ồn")

    def test_semantic_search_returns_empty_on_error(self, search_service, mock_es, mock_model):
        mock_es.search.side_effect = Exception("ES connection refused")
        results = search_service.semantic_search("test")
        assert results == []

    def test_result_has_required_fields(self, search_service, mock_es, mock_model):
        mock_es.search.return_value = {
            "hits": {
                "hits": [
                    {
                        "_score": 0.8,
                        "_source": {
                            "product_id": "P002",
                            "name": "Balo du lịch",
                            "description": "Balo chống nước",
                        },
                    }
                ]
            }
        }
        results = search_service.semantic_search("balo")
        assert all(k in results[0] for k in ("product_id", "name", "description", "score"))

    def test_uses_sample_product_fixture(self, search_service, sample_product):
        """Verify conftest sample_product fixture is accessible."""
        assert sample_product["product_id"] == "P001"
        assert "category" in sample_product
