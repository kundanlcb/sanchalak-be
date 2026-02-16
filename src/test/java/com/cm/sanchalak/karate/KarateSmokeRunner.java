package com.cm.sanchalak.karate;

import com.intuit.karate.junit5.Karate;

class KarateSmokeRunner {

    @Karate.Test
    Karate runSmoke() {
        return Karate.run("classpath:karate/modules")
                .tags("@smoke", "~@wip", "~@ignore");
    }
}
