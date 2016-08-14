/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * User avatar query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.1.3, Aug 14, 2016
 * @since 0.3.0
 */
@Service
public class AvatarQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AvatarQueryService.class.getName());

    /**
     * Default avatar URL.
     */
    private static final String DEFAULT_AVATAR_URL = Symphonys.get("defaultThumbnailURL");

    /**
     * Fills the specified user thumbnail URL.
     *
     * @param viewMode the specified view mode, {@code 0} for original image, {@code 1} for static image
     * @param user the specified user
     */
    public void fillUserAvatarURL(final int viewMode, final JSONObject user) {
        user.put(UserExt.USER_AVATAR_URL + "210", getAvatarURLByUser(viewMode, user, "210"));
        user.put(UserExt.USER_AVATAR_URL + "48", getAvatarURLByUser(viewMode, user, "48"));
        user.put(UserExt.USER_AVATAR_URL + "20", getAvatarURLByUser(viewMode, user, "20"));
    }

    /**
     * Gets the default avatar URL with the specified size.
     *
     * @param size the specified size
     * @return the default avatar URL
     */
    public String getDefaultAvatarURL(final String size) {
        final boolean qiniuEnabled = Symphonys.getBoolean("qiniu.enabled");
        if (qiniuEnabled) {
            return DEFAULT_AVATAR_URL + "?imageView2/1/w/" + size + "/h/" + size + "/interlace/0/q/100";
        } else {
            return DEFAULT_AVATAR_URL;
        }
    }

    /**
     * Gets the avatar URL for the specified user with the specified size.
     *
     * @param viewMode the specified view mode, {@code 0} for original image, {@code 1} for static image
     * @param user the specified user
     * @param size the specified size
     * @return the avatar URL
     */
    public String getAvatarURLByUser(final int viewMode, final JSONObject user, final String size) {
        if (null == user) {
            return DEFAULT_AVATAR_URL;
        }

        final boolean qiniuEnabled = Symphonys.getBoolean("qiniu.enabled");

        String originalURL = user.optString(UserExt.USER_AVATAR_URL);
        if (StringUtils.isBlank(originalURL)) {
            originalURL = DEFAULT_AVATAR_URL;
        }

        String avatarURL = StringUtils.substringBeforeLast(originalURL, "?");

        if (UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL == viewMode) {
            if (qiniuEnabled) {
                final String qiniuDomain = Symphonys.get("qiniu.domain");

                if (!StringUtils.startsWith(avatarURL, qiniuDomain)) {
                    return DEFAULT_AVATAR_URL + "?imageView2/1/w/" + size + "/h/" + size + "/interlace/0/q/100";
                } else {
                    return avatarURL + "?imageView2/1/w/" + size + "/h/" + size + "/interlace/0/q/100";
                }
            } else {
                return avatarURL;
            }
        } else if (qiniuEnabled) {
            final String qiniuDomain = Symphonys.get("qiniu.domain");

            if (!StringUtils.startsWith(avatarURL, qiniuDomain)) {
                return DEFAULT_AVATAR_URL + "?imageView2/1/w/" + size + "/h/" + size + "/format/jpg/interlace/0/q/100";
            } else {
                return avatarURL + "?imageView2/1/w/" + size + "/h/" + size + "/format/jpg/interlace/0/q/100";
            }
        } else {
            return avatarURL;
        }
    }

    /**
     * Creates a avatar image with the specified hash string and size.
     *
     * <p>
     * Refers to: https://github.com/superhj1987/awesome-identicon
     * </p>
     *
     * @param hash the specified hash string
     * @param size the specified size
     * @return buffered image
     */
    public BufferedImage createAvatar(final String hash, final int size) {
        final boolean[][] array = new boolean[6][5];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                array[i][j] = false;
            }
        }

        for (int i = 0; i < hash.length(); i += 2) {
            final int s = i / 2;

            final boolean v = Math.random() > 0.5;
            if (s % 3 == 0) {
                array[s / 3][0] = v;
                array[s / 3][4] = v;
            } else if (s % 3 == 1) {
                array[s / 3][1] = v;
                array[s / 3][3] = v;
            } else {
                array[s / 3][2] = v;
            }
        }

        final int ratio = Math.round(size / 5);

        final BufferedImage ret = new BufferedImage(ratio * 5, ratio * 5, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics graphics = ret.getGraphics();

        graphics.setColor(new Color(Integer.parseInt(String.valueOf(hash.charAt(0)), 16) * 16,
                Integer.parseInt(String.valueOf(hash.charAt(1)), 16) * 16,
                Integer.parseInt(String.valueOf(hash.charAt(2)), 16) * 16));
        graphics.fillRect(0, 0, ret.getWidth(), ret.getHeight());

        graphics.setColor(new Color(Integer.parseInt(String.valueOf(hash.charAt(hash.length() - 1)), 16) * 16,
                Integer.parseInt(String.valueOf(hash.charAt(hash.length() - 2)), 16) * 16,
                Integer.parseInt(String.valueOf(hash.charAt(hash.length() - 3)), 16) * 16));
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (array[i][j]) {
                    graphics.fillRect(j * ratio, i * ratio, ratio, ratio);
                }
            }
        }

        return ret;
    }
}
