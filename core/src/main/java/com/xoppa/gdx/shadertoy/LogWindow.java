package com.xoppa.gdx.shadertoy;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.ScrollableTextArea;
import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisWindow;

public class LogWindow extends VisWindow {
    private boolean collapsed;
    private float collapseHeight = 20f;
    private float expandHeight;
    private ScrollableTextArea textArea;

    public LogWindow(String title, float x, float y, float width, float height) {
        super(title);
        TableUtils.setSpacingDefaults(this);
        addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (getTapCount() == 2 && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
                    toggleCollapsed();
            }
        });
        setResizable(true);
        setSize(width, height);
        setPosition(x, y);
        addCloseButton();
        setColor(1.0f, 1.0f, 1.0f, 0.8f);

        textArea = new ScrollableTextArea("");
        textArea.getStyle().background = null;
        //todo: make select able, but not editable
        //textArea.setDisabled(true);
        add(new Separator()).fillX().row();
        add(textArea.createCompatibleScrollPane()).grow().fill().expand();
    }

    public void addTextAreaListener(EventListener listener) {
        textArea.addListener(listener);
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String value) {
        textArea.setText(value);
    }

    public void addText(String value) {
        setText(getText() + value);
        textArea.setCursorPosition(getText().length());
    }

    public void expand () {
        if (!collapsed) return;
        setHeight(expandHeight);
        setY(getY() - expandHeight + collapseHeight);
        collapsed = false;
    }

    public void collapse () {
        if (collapsed) return;
        expandHeight = getHeight();
        setHeight(collapseHeight);
        setY(getY() + expandHeight - collapseHeight);
        collapsed = true;
        if (getStage() != null) getStage().setScrollFocus(null);
    }

    public void toggleCollapsed () {
        if (collapsed)
            expand();
        else
            collapse();
    }

    public boolean isCollapsed () {
        return collapsed;
    }

}
