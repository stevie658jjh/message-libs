package com.color.sms.messages.theme.tools.linkPreview;

import android.text.TextPaint;
import android.text.style.URLSpan;

import org.jetbrains.annotations.NotNull;

public class URLSpanNoUnderline extends URLSpan {
    URLSpanNoUnderline(String p_Url) {
        super(p_Url);
    }

    public void updateDrawState(@NotNull TextPaint p_DrawState) {
        super.updateDrawState(p_DrawState);
        p_DrawState.setUnderlineText(false);
    }
}