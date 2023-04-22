package com.tripathysamapika.truecaller;


import orestes.bloomfilter.CountingBloomFilter;
import orestes.bloomfilter.FilterBuilder;

import static com.tripathysamapika.truecaller.model.common.Constant.MAX_GLOBAL_SPAM_COUNT;

public class GlobalSpam {
    private CountingBloomFilter<String> globalSpam =

            new FilterBuilder(MAX_GLOBAL_SPAM_COUNT, .01)
                    .buildCountingBloomFilter(
                    );

    private GlobalSpam() {
    }

    public static GlobalSpam INSTANCE = new GlobalSpam();

    public void reportSpam(String spamNumber, String reportingNumber, String reason) {
        System.out.println("Send metrics here for spam Number " + spamNumber +
                " reported " + reportingNumber + " for reason "+ reason);
       globalSpam.add(spamNumber);
    }

    public boolean isGlobalSpam(String number) {
        return globalSpam.contains(number);
    }
}
