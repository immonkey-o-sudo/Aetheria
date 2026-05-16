package com.jef.justenoughfakepixel.features.profile.viewer;

import com.jef.justenoughfakepixel.features.profile.data.ProfileData;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PlayerProfile {

    public String player_name;
    public int rating;
    public String updated_at;
    public String update_time;
    public List<ProfileData> profiles;

}