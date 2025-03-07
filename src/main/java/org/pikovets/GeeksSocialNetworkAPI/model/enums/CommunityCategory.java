package org.pikovets.GeeksSocialNetworkAPI.model.enums;

import lombok.Getter;

@Getter
public enum CommunityCategory {
    TECHNOLOGY("Technology"), HEALTH_WELLNESS("Health & Wellness"), FINANCE_BUSINESS("Finance & Business"), EDUCATION_LEARNING("Education & Learning"), ENTERTAINMENT("Entertainment"), SPORTS_FITNESS("Sports & Fitness"), TRAVEL_TOURISM("Travel & Tourism"), FOOD_CULINARY("Food & Culinary"), SCIENCE_RESEARCH("Science & Research"), AUTOMOTIVE("Automotive"), REAL_ESTATE("Real Estate"), FASHION_BEAUTY("Fashion & Beauty"), PARENTING_FAMILY("Parenting & Family"), GAMING("Gaming"), MUSIC_ARTS("Music & Arts"), BOOKS_LITERATURE("Books & Literature"), POLITICS_GOVERNMENT("Politics & Government"), ENVIRONMENT_SUSTAINABILITY("Environment & Sustainability"), DIY_CRAFTS("DIY & Crafts"), PETS_ANIMALS("Pets & Animals"), SPIRITUALITY_RELIGION("Spirituality & Religion"), HISTORY_CULTURE("History & Culture"), PHOTOGRAPHY("Photography"), MARKETING_ADVERTISING("Marketing & Advertising"), PRODUCTIVITY_SELF_IMPROVEMENT("Productivity & Self-Improvement");

    private final String categoryName;

    CommunityCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public String toString() {
        return categoryName;
    }
}