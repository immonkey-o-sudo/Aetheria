package io.hamlook.aetheria.features.profile.data.skills;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SkillData {

    public Skill skill;
    public int currentLevel;
    public long currentXp;
    public long requiredXp; // -1 at max level

}