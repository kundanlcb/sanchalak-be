package com.cm.sanchalak.karate;

import com.intuit.karate.junit5.Karate;

class KarateRegressionRunner {

    @Karate.Test
    Karate runRegression() {
        return Karate.run("classpath:karate/modules")
                .tags("@regression", "~@wip", "~@ignore");
    }
}
