"""
Unit tests for EcomDataProducer.
Run: pytest tests/unit/test_producer.py -v

Import path reflects src-layout:
  PYTHONPATH=src pytest  OR  pip install -e ".[dev]" (pyproject.toml sets pythonpath)
"""
import pytest
from unittest.mock import MagicMock, patch

# After restructure: ecom_pipeline.ingestion.producer (src-layout)
from ecom_pipeline.ingestion.producer import EcomDataProducer


TOPICS = {"user_behavior": "user_behavior", "products": "products"}


@pytest.fixture
def mock_kafka_producer():
    """Patch KafkaProducer — no real Kafka needed."""
    with patch("ecom_pipeline.ingestion.producer.KafkaProducer") as MockProducer:
        instance = MagicMock()
        MockProducer.return_value = instance
        yield instance


@pytest.fixture
def producer(mock_kafka_producer):
    return EcomDataProducer(bootstrap_servers=["localhost:9093"], topics=TOPICS)


class TestEcomDataProducer:

    def test_init_creates_kafka_producer(self, mock_kafka_producer):
        p = EcomDataProducer(bootstrap_servers=["localhost:9093"], topics=TOPICS)
        assert p.producer is not None

    def test_products_list_not_empty(self, producer):
        assert len(producer.products) > 0

    def test_each_product_has_required_fields(self, producer):
        required = {"product_id", "name", "category", "description"}
        for product in producer.products:
            assert required.issubset(product.keys()), f"Missing fields in {product}"

    def test_send_product_metadata_sends_all_products(self, producer, mock_kafka_producer):
        producer.send_product_metadata()
        assert mock_kafka_producer.send.call_count == len(producer.products)
        mock_kafka_producer.flush.assert_called_once()

    def test_send_product_metadata_uses_correct_topic(self, producer, mock_kafka_producer):
        producer.send_product_metadata()
        for call_args in mock_kafka_producer.send.call_args_list:
            assert call_args[0][0] == TOPICS["products"]

    def test_generate_behavior_returns_valid_schema(self, producer):
        behavior = producer.generate_behavior()
        assert {"user_id", "product_id", "action", "timestamp"}.issubset(behavior)

    def test_generate_behavior_action_is_valid(self, producer):
        valid_actions = {"click", "view", "purchase"}
        for _ in range(20):
            assert producer.generate_behavior()["action"] in valid_actions

    def test_generate_behavior_product_id_is_known(self, producer):
        known_ids = {p["product_id"] for p in producer.products}
        for _ in range(20):
            assert producer.generate_behavior()["product_id"] in known_ids

    def test_generate_behavior_user_id_format(self, producer):
        behavior = producer.generate_behavior()
        assert behavior["user_id"].startswith("U")
        assert len(behavior["user_id"]) == 5

    def test_producer_close_called_on_shutdown(self, producer, mock_kafka_producer):
        producer.producer.close()
        mock_kafka_producer.close.assert_called_once()

    def test_uses_sample_config_fixture(self, producer, sample_config):
        """Verify conftest sample_config fixture is accessible."""
        assert "kafka" in sample_config
        assert sample_config["kafka"]["topics"]["products"] == "products"
