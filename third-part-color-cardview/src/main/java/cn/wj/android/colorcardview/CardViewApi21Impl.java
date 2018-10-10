/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.wj.android.colorcardview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;

@RequiresApi(21)
class CardViewApi21Impl extends CardViewApi17Impl {

    private boolean useLower = false;

    @Override
    public void initialize(CardViewDelegate cardView, Context context,
                           ColorStateList backgroundColor, float radius, float elevation, float maxElevation,
                           ColorStateList shadowColorStart, ColorStateList shadowColorEnd) {

        if (shadowColorStart == null && shadowColorEnd == null) {
            useLower = false;

            final RoundRectDrawable background = new RoundRectDrawable(backgroundColor, radius);
            cardView.setCardBackground(background);

            View view = cardView.getCardView();
            view.setClipToOutline(true);
            view.setElevation(elevation);
            setMaxElevation(cardView, maxElevation);
        } else {
            useLower = true;
            super.initialize(cardView, context, backgroundColor, radius, elevation, maxElevation, shadowColorStart, shadowColorEnd);
        }
    }

    @Override
    public void setRadius(CardViewDelegate cardView, float radius) {
        if (useLower) {
            super.setRadius(cardView, radius);
        } else {
            getCardBackground(cardView).setRadius(radius);
        }
    }

    @Override
    public void setMaxElevation(CardViewDelegate cardView, float maxElevation) {
        if (useLower) {
            super.setMaxElevation(cardView, maxElevation);
        } else {
            getCardBackground(cardView).setPadding(maxElevation,
                    cardView.getUseCompatPadding(), cardView.getPreventCornerOverlap());
            updatePadding(cardView);
        }
    }

    @Override
    public float getMaxElevation(CardViewDelegate cardView) {
        if (useLower) {
            return super.getMaxElevation(cardView);
        } else {
            return getCardBackground(cardView).getPadding();
        }
    }

    @Override
    public float getMinWidth(CardViewDelegate cardView) {
        if (useLower) {
            return super.getMinWidth(cardView);
        } else {
            return getRadius(cardView) * 2;
        }
    }

    @Override
    public float getMinHeight(CardViewDelegate cardView) {
        if (useLower) {
            return super.getMinHeight(cardView);
        } else {
            return getRadius(cardView) * 2;
        }
    }

    @Override
    public float getRadius(CardViewDelegate cardView) {
        if (useLower) {
            return super.getRadius(cardView);
        } else {
            return getCardBackground(cardView).getRadius();
        }
    }

    @Override
    public void setElevation(CardViewDelegate cardView, float elevation) {
        if (useLower) {
            super.setElevation(cardView, elevation);
        } else {
            cardView.getCardView().setElevation(elevation);
        }
    }

    @Override
    public float getElevation(CardViewDelegate cardView) {
        if (useLower) {
            return super.getElevation(cardView);
        } else {
            return cardView.getCardView().getElevation();
        }
    }

    @Override
    public void updatePadding(CardViewDelegate cardView) {
        if (useLower) {
            super.updatePadding(cardView);
        } else {
            if (!cardView.getUseCompatPadding()) {
                cardView.setShadowPadding(0, 0, 0, 0);
                return;
            }
            float elevation = getMaxElevation(cardView);
            final float radius = getRadius(cardView);
            int hPadding = (int) Math.ceil(RoundRectDrawableWithShadow
                    .calculateHorizontalPadding(elevation, radius, cardView.getPreventCornerOverlap()));
            int vPadding = (int) Math.ceil(RoundRectDrawableWithShadow
                    .calculateVerticalPadding(elevation, radius, cardView.getPreventCornerOverlap()));
            cardView.setShadowPadding(hPadding, vPadding, hPadding, vPadding);
        }
    }

    @Override
    public void onCompatPaddingChanged(CardViewDelegate cardView) {
        if (useLower) {
            super.onCompatPaddingChanged(cardView);
        } else {
            setMaxElevation(cardView, getMaxElevation(cardView));
        }
    }

    @Override
    public void onPreventCornerOverlapChanged(CardViewDelegate cardView) {
        if (useLower) {
            super.onPreventCornerOverlapChanged(cardView);
        } else {
            setMaxElevation(cardView, getMaxElevation(cardView));
        }
    }

    @Override
    public void setBackgroundColor(CardViewDelegate cardView, @Nullable ColorStateList color) {
        if (useLower) {
            super.setBackgroundColor(cardView, color);
        } else {
            getCardBackground(cardView).setColor(color);
        }
    }

    @Override
    public ColorStateList getBackgroundColor(CardViewDelegate cardView) {
        if (useLower) {
            return super.getBackgroundColor(cardView);
        } else {
            return getCardBackground(cardView).getColor();
        }
    }

    private RoundRectDrawable getCardBackground(CardViewDelegate cardView) {
        return ((RoundRectDrawable) cardView.getCardBackground());
    }
}
