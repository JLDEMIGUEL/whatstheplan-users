package com.whatstheplan.users.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
    SOCCER("Soccer"),
    BASKETBALL("Basketball"),
    TENNIS("Tennis"),
    SWIMMING("Swimming"),
    RUNNING("Running"),
    CYCLING("Cycling"),
    GOLF("Golf"),
    BASEBALL("Baseball"),
    MARTIAL_ARTS("Martial Arts"),
    YOGA("Yoga"),
    SNOWBOARDING("Snowboarding"),
    CLIMBING("Climbing"),
    MUSIC("Music"),
    ARTS("Arts"),
    TECH("Technology"),
    EDUCATION("Education"),
    OUTDOORS("Outdoors"),
    FOOD("Food & Dining"),
    SOCIAL("Social Events"),
    WELLNESS("Wellness & Fitness"),
    NETWORKING("Networking"),
    GAMING("Gaming"),
    TRAVEL("Travel"),
    VOLUNTEERING("Volunteering"),
    SHOPPING("Shopping"),
    READING("Reading"),
    WRITING("Writing"),
    PHOTOGRAPHY("Photography"),
    GARDENING("Gardening"),
    COOKING("Cooking"),
    BAKING("Baking"),
    FASHION("Fashion & Style"),
    FILM("Film & Movies"),
    FITNESS("Fitness & Bodybuilding"),
    MEDITATION("Meditation & Mindfulness"),
    FISHING("Fishing"),
    HIKING("Hiking"),
    BOARD_GAMES("Board Games"),
    DANCING("Dancing"),
    LANGUAGE_LEARNING("Language Learning"),
    PAINTING("Painting");

    private final String displayName;
}

