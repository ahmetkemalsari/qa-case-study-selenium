package com.qa.tests;

import com.qa.utils.LoggerManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseTest {

    protected final Logger logger = LoggerManager.getLogger(getClass());

    @BeforeEach
    void setUpTest() {
        logger.info("Test setup started.");
    }

    @AfterEach
    void tearDownTest() {
        logger.info("Test teardown completed.");
    }
}
