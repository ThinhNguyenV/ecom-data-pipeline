"""
logging_config.py
-----------------
Centralised logging configuration for ecom_pipeline.

Usage:
    from ecom_pipeline.common.logging_config import setup_logging
    setup_logging(level="INFO", json_format=True)
"""

import logging
import sys
from typing import Optional


def setup_logging(
    level: str = "INFO",
    json_format: bool = False,
    logger_name: Optional[str] = None,
) -> logging.Logger:
    """
    Configure root (or named) logger with structured output.

    Args:
        level:        Logging level — DEBUG | INFO | WARNING | ERROR
        json_format:  If True, emit JSON lines (suitable for log aggregators)
        logger_name:  If None, configures the root logger

    Returns:
        Configured Logger instance
    """
    log_level = getattr(logging, level.upper(), logging.INFO)

    if json_format:
        fmt = (
            '{"time":"%(asctime)s","level":"%(levelname)s",'
            '"logger":"%(name)s","message":"%(message)s"}'
        )
    else:
        fmt = "%(asctime)s [%(levelname)-8s] %(name)s — %(message)s"

    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(logging.Formatter(fmt, datefmt="%Y-%m-%dT%H:%M:%S"))

    logger = logging.getLogger(logger_name)
    logger.setLevel(log_level)

    # Avoid duplicate handlers if called multiple times
    if not logger.handlers:
        logger.addHandler(handler)

    # Silence noisy third-party loggers
    for noisy in ("kafka", "urllib3", "elasticsearch", "pyspark"):
        logging.getLogger(noisy).setLevel(logging.WARNING)

    return logger


# Module-level convenience logger
logger = setup_logging(logger_name="ecom_pipeline")
