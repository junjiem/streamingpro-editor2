/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jsyntaxpane.actions;

import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.util.Configuration;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.event.ActionEvent;

public class JUnindentAction extends TextAction implements SyntaxAction {

    /**
     * creates new JUnindentAction.
     * Initial Code contributed by ser... AT mail.ru
     */
    public JUnindentAction() {
        super("JUNINDENT");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
            SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(target);
            int pos = target.getCaretPosition();
            int start = sDoc.getParagraphElement(pos).getStartOffset();
            String line = ActionUtils.getLine(target);
            if (ActionUtils.isEmptyOrBlanks(line)) {
                try {
                    sDoc.insertString(pos, "}", null);
                    Token t = sDoc.getPairFor(sDoc.getTokenAt(pos));
                    if (null != t) {
                        String pairLine = ActionUtils.getLineAt(target, t.start);
                        String indent = ActionUtils.getIndent(pairLine);
                        sDoc.replace(start, line.length() + 1, indent + "}", null);
                    }
                } catch (BadLocationException ble) {
                    target.replaceSelection("}");
                }
            } else {
                target.replaceSelection("}");
            }
        }
    }

    public void config(Configuration config, String prefix, String name) {
    }

    public TextAction getAction(String key) {
        return this;
    }
}
