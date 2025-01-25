package com.chromatics.caller_id.common;

import com.chromatics.caller_id.data.db.BlacklistItem;
import com.chromatics.caller_id.models.CommunityDatabaseItem;
import com.chromatics.caller_id.models.FeaturedDatabaseItem;


public class NumberInfo {

    public enum BlockingReason {
        HIDDEN_NUMBER, SIA_RATING, BLACKLISTED
    }

    public enum Rating {
        POSITIVE, NEUTRAL, NEGATIVE, UNKNOWN
    }

    // id
    public String number;
    public String normalizedNumber;

    // info from various sources
    public boolean isHiddenNumber;
    public ContactItem contactItem;
    public CommunityDatabaseItem communityDatabaseItem;
    public FeaturedDatabaseItem featuredDatabaseItem;
    public BlacklistItem blacklistItem;

    // computed rating
    public Rating rating = Rating.UNKNOWN;

    // precomputed for convenience
    public boolean noNumber;
    public String name;
    public BlockingReason blockingReason;

}
