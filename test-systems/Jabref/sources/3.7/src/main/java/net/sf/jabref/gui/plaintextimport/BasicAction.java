package net.sf.jabref.gui.plaintextimport;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

abstract class BasicAction extends AbstractAction {

    public BasicAction(String text, String description, Icon icon) {
        super(text, icon);
        putValue(Action.SHORT_DESCRIPTION, description);
    }

    public BasicAction(String text) {
        super(text);
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);
}
