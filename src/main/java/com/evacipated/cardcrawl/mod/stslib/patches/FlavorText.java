package com.evacipated.cardcrawl.mod.stslib.patches;

import basemod.ReflectionHacks;
import basemod.patches.com.megacrit.cardcrawl.helpers.TipHelper.FakeKeywords;
import basemod.patches.com.megacrit.cardcrawl.screens.SingleCardViewPopup.ScrollingTooltips;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.google.gson.annotations.SerializedName;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.*;

import java.util.ArrayList;

import static basemod.patches.com.megacrit.cardcrawl.screens.SingleCardViewPopup.TitleFontSize.fontFile;

public class FlavorText {
    private static final BitmapFont flavorFont = FlavorText.prepFont(22.0f, false);

    private static final String TIP_TOP_STRING = "images/stslib/ui/tipTop.png";
    private static final String TIP_MID_STRING = "images/stslib/ui/tipMid.png";
    private static final String TIP_BOT_STRING = "images/stslib/ui/tipBot.png";

    public static Texture TIP_TOP;
    public static Texture TIP_MID;
    public static Texture TIP_BOT;

    public static AbstractCard card;
    public static AbstractPotion potion;
    public static RewardItem item;

    private static float BODY_TEXT_WIDTH;
    private static float TIP_DESC_LINE_SPACING;
    private static float SHADOW_DIST_X;
    private static float SHADOW_DIST_Y;
    private static float BOX_EDGE_H;
    private static float BOX_BODY_H;
    private static float BOX_W;
    private static float TEXT_OFFSET_X;

    public enum boxType {
        WHITE,
        TRADITIONAL,
        CUSTOM
    }

    private static void setConstants() {
        BODY_TEXT_WIDTH = ReflectionHacks.getPrivate(null, TipHelper.class, "BODY_TEXT_WIDTH");
        TIP_DESC_LINE_SPACING = ReflectionHacks.getPrivate(null, TipHelper.class, "TIP_DESC_LINE_SPACING");
        SHADOW_DIST_X = ReflectionHacks.getPrivate(null, TipHelper.class, "SHADOW_DIST_X");
        SHADOW_DIST_Y = ReflectionHacks.getPrivate(null, TipHelper.class, "SHADOW_DIST_Y");
        BOX_EDGE_H = ReflectionHacks.getPrivate(null, TipHelper.class, "BOX_EDGE_H");
        BOX_BODY_H = ReflectionHacks.getPrivate(null, TipHelper.class, "BOX_BODY_H");
        BOX_W = ReflectionHacks.getPrivate(null, TipHelper.class, "BOX_W");
        TEXT_OFFSET_X = ReflectionHacks.getPrivate(null, TipHelper.class, "TEXT_OFFSET_X");
    }

    private static float addFlavorText(float x, float y, SpriteBatch sb, AbstractPotion potion) {
        if (potion == null)
            return y;

        Color boxColor;
        Color textColor;
        String s;

        s = PotionFlavorFields.flavor.get(potion);
        boxColor = PotionFlavorFields.boxColor.get(potion);
        textColor = PotionFlavorFields.textColor.get(potion);

        if (TIP_BOT == null || TIP_MID == null || TIP_TOP == null)
            setTextures();

        Texture topTexture = TIP_TOP;
        Texture midTexture = TIP_MID;
        Texture botTexture = TIP_BOT;

        if (PotionFlavorFields.flavorBoxType.get(potion) == boxType.TRADITIONAL) {
            topTexture = ImageMaster.KEYWORD_TOP;
            midTexture = ImageMaster.KEYWORD_BODY;
            botTexture = ImageMaster.KEYWORD_BOT;
        }
        else if (PotionFlavorFields.flavorBoxType.get(potion) == boxType.CUSTOM &&
                PotionFlavorFields.boxTop.get(potion) != null &&
                PotionFlavorFields.boxMid.get(potion) != null &&
                PotionFlavorFields.boxBot.get(potion) != null)
        {
            topTexture = PotionFlavorFields.boxTop.get(potion);
            midTexture = PotionFlavorFields.boxMid.get(potion);
            botTexture = PotionFlavorFields.boxBot.get(potion);
        }

        return addFlavorText(x, y, sb, s, boxColor, textColor, topTexture, midTexture, botTexture);
    }

    private static float addFlavorText(float x, float y, SpriteBatch sb, AbstractCard card) {
        if (card == null)
            return y;

        Color boxColor;
        Color textColor;
        String s;

        s = AbstractCardFlavorFields.flavor.get(card);
        boxColor = AbstractCardFlavorFields.boxColor.get(card);
        textColor = AbstractCardFlavorFields.textColor.get(card);

        if (TIP_BOT == null || TIP_MID == null || TIP_TOP == null)
            setTextures();

        Texture topTexture = TIP_TOP;
        Texture midTexture = TIP_MID;
        Texture botTexture = TIP_BOT;

        if (AbstractCardFlavorFields.flavorBoxType.get(card) == boxType.TRADITIONAL) {
            topTexture = ImageMaster.KEYWORD_TOP;
            midTexture = ImageMaster.KEYWORD_BODY;
            botTexture = ImageMaster.KEYWORD_BOT;
        }
        else if (AbstractCardFlavorFields.flavorBoxType.get(card) == boxType.CUSTOM &&
                AbstractCardFlavorFields.boxTop.get(card) != null &&
                AbstractCardFlavorFields.boxMid.get(card) != null &&
                AbstractCardFlavorFields.boxBot.get(card) != null)
        {
            topTexture = AbstractCardFlavorFields.boxTop.get(card);
            midTexture = AbstractCardFlavorFields.boxMid.get(card);
            botTexture = AbstractCardFlavorFields.boxBot.get(card);
        }

        return addFlavorText(x, y, sb, s, boxColor, textColor, topTexture, midTexture, botTexture);
    }

    private static float addFlavorText(float x, float y, SpriteBatch sb, String s, Color boxColor, Color textColor,
                                       Texture topTexture, Texture midTexture, Texture botTexture) {
        if (BODY_TEXT_WIDTH == 0)
            setConstants();

        if (boxColor == null || s == null || s.equals("") || textColor == null)
            return y;

        float h = -FontHelper.getSmartHeight(flavorFont, s, BODY_TEXT_WIDTH, TIP_DESC_LINE_SPACING)
                - 40.0F * Settings.scale;

        sb.setColor(Settings.TOP_PANEL_SHADOW_COLOR);
        sb.draw(ImageMaster.KEYWORD_TOP, x + SHADOW_DIST_X, y - SHADOW_DIST_Y, BOX_W, BOX_EDGE_H);
        sb.draw(ImageMaster.KEYWORD_BODY, x + SHADOW_DIST_X, y - h - BOX_EDGE_H - SHADOW_DIST_Y,
                BOX_W, h + BOX_EDGE_H);
        sb.draw(ImageMaster.KEYWORD_BOT, x + SHADOW_DIST_X, y - h - BOX_BODY_H - SHADOW_DIST_Y,
                BOX_W, BOX_EDGE_H);
        sb.setColor(boxColor.cpy());
        sb.draw(topTexture, x, y, BOX_W, BOX_EDGE_H);
        sb.draw(midTexture, x, y - h - BOX_EDGE_H, BOX_W, h + BOX_EDGE_H);
        sb.draw(botTexture, x, y - h - BOX_BODY_H, BOX_W, BOX_EDGE_H);

        FontHelper.renderSmartText(sb, flavorFont, s,x + TEXT_OFFSET_X,
                y + 13.0F * Settings.scale, BODY_TEXT_WIDTH, TIP_DESC_LINE_SPACING,
                textColor);

        y -= h + BOX_EDGE_H * 3.15F;

        return y;
    }

    @SpirePatch2(
            clz = TipHelper.class,
            method = "renderKeywords"
    )
    public static class TipHelperRenderFlavorPatch{
        @SpirePostfixPatch
        public static void TipHelperRenderFlavor(float x, @ByRef float[] y, SpriteBatch sb, AbstractCard ___card) {
            y[0] = addFlavorText(x, y[0], sb, ___card);
        }
    }

    @SpirePatch2(
            clz = FakeKeywords.class,
            method = "Prefix"
    )
    public static class IHeardYouLikePatchesSoIPutAPatchInYourPatch {
        @SpireInsertPatch(
                locator = Locator.class,
                localvars = {"sumTooltipHeight"}
        )
        public static void insertPatch(float x, SpriteBatch sb, AbstractCard ___card,
                                       float ___BODY_TEXT_WIDTH, float ___TIP_DESC_LINE_SPACING,
                                       float ___BOX_EDGE_H, @ByRef float[] sumTooltipHeight) {
            String s = AbstractCardFlavorFields.flavor.get(___card);
            if (s == null || s.equals(""))
                return;

            float textHeight = -FontHelper.getSmartHeight(flavorFont, s, ___BODY_TEXT_WIDTH, ___TIP_DESC_LINE_SPACING)
                    - 40.0F * Settings.scale;
            sumTooltipHeight[0] += textHeight + ___BOX_EDGE_H * 3.15F;
        }
        private static class Locator extends SpireInsertLocator {
            private Locator() {}
            public int[] Locate(CtBehavior behavior) throws Exception {
                Matcher matcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "IMG_HEIGHT");
                return LineFinder.findInOrder(behavior, matcher);
            }
        }
    }

    @SpirePatch2(
            clz = ScrollingTooltips.class,
            method = "powerTipsHeight"
    )
    public static class PatchInPatchTwoElectricBoogaloo {
        @SpirePostfixPatch
        public static float postfix(float __result, ArrayList<PowerTip> powerTips) {
            if (card == null)
                return __result;
            String s = AbstractCardFlavorFields.flavor.get(card);
            if (s == null || s.equals(""))
                return __result;

            float BOX_EDGE_H = ReflectionHacks.getPrivate(null, ScrollingTooltips.class,
                    "BOX_EDGE_H");
            float BODY_TEXT_WIDTH = ReflectionHacks.getPrivate(null, ScrollingTooltips.class,
                    "BODY_TEXT_WIDTH");
            float TIP_DESC_LINE_SPACING = ReflectionHacks.getPrivate(null, ScrollingTooltips.class,
                    "TIP_DESC_LINE_SPACING");

            float textHeight = -FontHelper.getSmartHeight(flavorFont, s, BODY_TEXT_WIDTH, TIP_DESC_LINE_SPACING)
                    - 40.0F * Settings.scale;
            __result += textHeight + BOX_EDGE_H * 3.15F;
            return __result;
        }
    }

    @SpirePatch2(
            clz = SingleCardViewPopup.class,
            method = "renderTips"
    )
    public static class PassEmptyTooltips {
        @SpireInsertPatch(
                locator = Locator.class,
                localvars = {"t"}
        )
        public static void Insert(ArrayList<PowerTip> t) {
            if (t.isEmpty())
                TipHelper.queuePowerTips((float)Settings.WIDTH / 2.0F + 340.0F * Settings.scale, 420.0F * Settings.scale, t);
        }
        private static class Locator extends SpireInsertLocator {
            private Locator() {}

            @Override
            public int[] Locate(CtBehavior behavior) throws Exception {
                Matcher matcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "cardsToPreview");
                return LineFinder.findInOrder(behavior, matcher);
            }
        }
    }

    @SpirePatch2(
            clz = SingleCardViewPopup.class,
            method = "open",
            paramtypez = { AbstractCard.class, CardGroup.class }
    )
    public static class CatchOpen {
        @SpirePrefixPatch
        public static void prefix(AbstractCard card) {
            FlavorText.card = card;
        }
    }

    @SpirePatch2(
            clz = SingleCardViewPopup.class,
            method = "close"
    )
    public static class CatchClose {
        @SpirePrefixPatch
        public static void prefix() {
            FlavorText.card = null;
        }
    }

    @SpirePatch2(
            clz = AbstractPotion.class,
            method = "labRender"
    )
    @SpirePatch2(
            clz = AbstractPotion.class,
            method = "shopRender"
    )
    public static class PassPotionTooltips {
        @SpireInsertPatch(
                locator = Locator.class
        )
        public static void Insert(AbstractPotion __instance) {
            FlavorText.potion = __instance;
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior behavior) throws Exception {
                Matcher matcher = new Matcher.MethodCallMatcher(TipHelper.class, "queuePowerTips");
                return LineFinder.findInOrder(behavior, matcher);
            }
        }
    }

    @SpirePatch2(
            clz = TopPanel.class,
            method = "renderPotionTips"
    )
    public static class PassPotionPanelTooltips {
        @SpireInsertPatch(
                locator = Locator.class,
                localvars = {"p"}
        )
        public static void Insert(TopPanel __instance, AbstractPotion p) {
            potion = p;
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior behavior) throws Exception {
                Matcher matcher = new Matcher.MethodCallMatcher(TipHelper.class, "queuePowerTips");
                return LineFinder.findInOrder(behavior, matcher);
            }
        }
    }

    @SpirePatch2(
            clz = RewardItem.class,
            method = "update"
    )
    public static class PassPotionRewardTooltips {
        @SpireInsertPatch(
                locator = Locator.class
        )
        public static void Insert(RewardItem __instance) {
            item = __instance;
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior behavior) throws Exception {
                Matcher matcher = new Matcher.MethodCallMatcher(TipHelper.class, "queuePowerTips");
                return LineFinder.findInOrder(behavior, matcher);
            }
        }
    }

    @SpirePatch2(
            clz = TipHelper.class,
            method = "renderPowerTips"
    )
    public static class TipHelperRenderFlavorPowerTips {
        @SpirePrefixPatch
        public static void Prefix(@ByRef float[] y, ArrayList<PowerTip> powerTips) {
            if (potion == null || !potion.hb.hovered)
                return;
            if (BODY_TEXT_WIDTH == 0)
                setConstants();

            float altY = TipHelper.calculateToAvoidOffscreen(powerTips, 0f);
            String flavorText = PotionFlavorFields.flavor.get(potion);
            if (flavorText != null && !flavorText.equals(""))
                altY += FontHelper.getSmartHeight(flavorFont, flavorText, BODY_TEXT_WIDTH, TIP_DESC_LINE_SPACING)
                        + 40.0F * Settings.scale;
            y[0] = Math.max(altY, y[0]);
        }

        @SpirePostfixPatch
        public static void Postfix(float x, @ByRef float[] y, SpriteBatch sb) {
            if (card != null)
                y[0] = addFlavorText(x, y[0], sb, card);
            else if (potion != null) {
                if (potion.hb.hovered)
                    y[0] = addFlavorText(x, y[0], sb, potion);
                else
                    potion = null;
            }
            else if (item != null) {
                if (item.hb.hovered && item.type == RewardItem.RewardType.POTION)
                    y[0] = addFlavorText(x, y[0], sb, item.potion);
                else
                    item = null;
            }
        }
    }

    @SpirePatch(
            clz = CardStrings.class,
            method = SpirePatch.CLASS
    )
    public static class CardStringsFlavorField {
        @SerializedName("FLAVOR")
        public static SpireField<String> cardFlavor = new SpireField<>(() -> "");
    }

    @SpirePatch(
            clz = PotionStrings.class,
            method = SpirePatch.CLASS
    )
    public static class PotionStringsFlavorField {
        @SerializedName("FLAVOR")
        public static SpireField<String> potionFlavor = new SpireField<>(() -> "");
    }

    @SpirePatch2(
            clz = AbstractCard.class,
            method = SpirePatch.CLASS
    )
    public static class AbstractCardFlavorFields {
        public static SpireField<String> flavor = new SpireField<>(() -> null);
        public static SpireField<Color> boxColor = new SpireField<>(Color.WHITE::cpy);
        public static SpireField<Color> textColor = new SpireField<>(Color.BLACK::cpy);
        public static SpireField<boxType> flavorBoxType = new SpireField<>(() -> boxType.WHITE);
        public static SpireField<Texture> boxTop = new SpireField<>(() -> null);
        public static SpireField<Texture> boxMid = new SpireField<>(() -> null);
        public static SpireField<Texture> boxBot = new SpireField<>(() -> null);
    }

    @SpirePatch2(
            clz = AbstractPotion.class,
            method = SpirePatch.CLASS
    )
    public static class PotionFlavorFields {
        public static SpireField<String> flavor = new SpireField<>(() -> null);
        public static SpireField<Color> boxColor = new SpireField<>(Color.WHITE::cpy);
        public static SpireField<Color> textColor = new SpireField<>(Color.BLACK::cpy);
        public static SpireField<boxType> flavorBoxType = new SpireField<>(() -> boxType.WHITE);
        public static SpireField<Texture> boxTop = new SpireField<>(() -> null);
        public static SpireField<Texture> boxMid = new SpireField<>(() -> null);
        public static SpireField<Texture> boxBot = new SpireField<>(() -> null);
    }

    @SpirePatch2(
            clz = AbstractCard.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, String.class, int.class, String.class,
                    AbstractCard.CardType.class, AbstractCard.CardColor.class, AbstractCard.CardRarity.class,
                    AbstractCard.CardTarget.class, DamageInfo.DamageType.class}
    )
    public static class FlavorIntoCardStrings {
        @SpirePostfixPatch
        public static void postfix(AbstractCard __instance) {
            CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(__instance.cardID);
            if (cardStrings == null || CardStringsFlavorField.cardFlavor.get(cardStrings) == null)
                return;

            AbstractCardFlavorFields.flavor.set(__instance, CardStringsFlavorField.cardFlavor.get(cardStrings));
        }
    }

    @SpirePatch2(
            clz = AbstractPotion.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, AbstractPotion.PotionRarity.class, AbstractPotion.PotionSize.class,
                    AbstractPotion.PotionColor.class}
    )
    @SpirePatch2(
            clz = AbstractPotion.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {String.class, String.class, AbstractPotion.PotionRarity.class, AbstractPotion.PotionSize.class,
                    AbstractPotion.PotionEffect.class, Color.class, Color.class, Color.class}
    )
    public static class FlavorIntoPotionStrings {
        @SpirePostfixPatch
        public static void postfix(AbstractPotion __instance) {
            PotionStrings potionStrings = CardCrawlGame.languagePack.getPotionString(__instance.ID);
            if (potionStrings == null || PotionStringsFlavorField.potionFlavor.get(potionStrings) == null)
                return;

            PotionFlavorFields.flavor.set(__instance, PotionStringsFlavorField.potionFlavor.get(potionStrings));
        }
    }

    // We want tipBodyFont but without shadows basically
    public static BitmapFont prepFont(float size, boolean isLinearFiltering) {
        FreeTypeFontGenerator g = new FreeTypeFontGenerator(fontFile);

        if (Settings.BIG_TEXT_MODE) {
            size *= 1.2F;
        }

        float fontScale = 1.0f;

        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.hinting = FreeTypeFontGenerator.Hinting.Slight;
        p.characters = "";
        p.incremental = true;
        p.size = Math.round(size * fontScale * Settings.scale);
        p.gamma = 0.9f;
        p.spaceX = 0;
        p.spaceY = 0;
        p.borderStraight = false;
        p.borderGamma = 0.9F;
        p.borderColor = new Color(0, 0, 0, 1);
        p.borderWidth = 0.0F;
        p.shadowColor = new Color(0, 0, 0, 0);
        p.shadowOffsetX = 0;
        p.shadowOffsetY = 0;
        if (isLinearFiltering) {
            p.minFilter = Texture.TextureFilter.Linear;
            p.magFilter = Texture.TextureFilter.Linear;
        } else {
            p.minFilter = Texture.TextureFilter.Nearest;
            p.magFilter = Texture.TextureFilter.MipMapLinearNearest;
        }

        g.scaleForPixelHeight(p.size);
        BitmapFont font = g.generateFont(p);
        font.setUseIntegerPositions(!isLinearFiltering);
        font.getData().markupEnabled = true;
        if (LocalizedStrings.break_chars != null) {
            font.getData().breakChars = LocalizedStrings.break_chars.toCharArray();
        }

        font.getData().fontFile = fontFile;
        return font;
    }

    private static void setTextures() {
        TIP_TOP = new Texture(TIP_TOP_STRING);
        TIP_MID = new Texture(TIP_MID_STRING);
        TIP_BOT = new Texture(TIP_BOT_STRING);
    }
}