package com.cm.sanchalak.karate;

import com.intuit.karate.junit5.Karate;

class KarateJourneyRunner {

    @Karate.Test
    Karate runJourney() {
        return Karate.run("classpath:karate/journeys")
                .tags("@journey", "~@wip", "~@ignore");
    }
}
