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

import jsyntaxpane.components.Markers;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A Find and Replace Dialog.  The dialog will also act as a listener to
 * Document changes so that all highlights are updated if the document is
 * changed.
 * 
 * @author Ayman Al-Sairafi
 */
public class ReplaceDialog extends javax.swing.JDialog implements CaretListener, KeyListener, ContainerListener {

    private JTextComponent textComponent;
    private FindReplaceActions finder;
    private static Markers.SimpleMarker SEARCH_MARKER = new Markers.SimpleMarker(Color.YELLOW);

    /** Creates new form FindDialog */
    public ReplaceDialog(JTextComponent text,
            FindReplaceActions finderActions) {
        super(ActionUtils.getFrameFor(text), false);
        initComponents();
        registerKeyAction(this);
        textComponent = text;
        finder = finderActions;
        textComponent.addCaretListener(this);
        setLocationRelativeTo(text.getRootPane());
    }

    /**
     * updates the highlights in the document when it is updated.
     * This is called by the DocumentListener methods
     */
    public void updateHighlights() {
        Markers.removeMarkers(textComponent, SEARCH_MARKER);
        if (jTglHighlight.isSelected()) {
            Markers.markAll(textComponent, finder.getPattern(), SEARCH_MARKER);
        }
    }

    private void showRegexpError(PatternSyntaxException ex) throws HeadlessException {
        JOptionPane.showMessageDialog(this, "Regexp error: " + ex.getMessage(),
                "Regular Expression Error", JOptionPane.ERROR_MESSAGE);
        jCmbFind.requestFocus();
    }

    /**
     * update the finder object with data from our UI
     */
    private void updateFinder() {
        int flag = 0;
        if (!jChkRegex.isSelected()) {
            flag |= Pattern.LITERAL;
        }
        flag |= (jChkIgnoreCase.isSelected()) ? Pattern.CASE_INSENSITIVE : 0;
        if (jChkIgnoreCase.isSelected()) {
            flag |= Pattern.CASE_INSENSITIVE;
        }
        String regex = (String) jCmbFind.getSelectedItem();
        if (regex != null && regex.length() > 0) {
            Pattern pattern = Pattern.compile(regex, flag);
            finder.setWrap(jChkWrap.isSelected());
            finder.setPattern(pattern);
            ActionUtils.insertIntoCombo(jCmbFind, regex);
        } else {
            finder.setPattern(null);
        }
    }

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jBtnNext = new javax.swing.JButton();
        jLblStatus = new javax.swing.JLabel();
        jBtnReplaceAll = new javax.swing.JButton();
        jChkWrap = new javax.swing.JCheckBox();
        jChkRegex = new javax.swing.JCheckBox();
        jChkIgnoreCase = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jTglHighlight = new javax.swing.JToggleButton();
        jCmbReplace = new javax.swing.JComboBox();
        jCmbFind = new javax.swing.JComboBox();

        setTitle("Find and Replace");
        setName(""); // NOI18N
        setResizable(false);

        jLabel1.setText("Find");

        jBtnNext.setMnemonic('N');
        jBtnNext.setText("Next");
        jBtnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNextActionPerformed(evt);
            }
        });

        jBtnReplaceAll.setMnemonic('H');
        jBtnReplaceAll.setText("Replace All");
        jBtnReplaceAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnReplaceAllActionPerformed(evt);
            }
        });

        jChkWrap.setMnemonic('W');
        jChkWrap.setText("Wrap around");
        jChkWrap.setToolTipText("Wrap to beginning when end is reached");

        jChkRegex.setMnemonic('R');
        jChkRegex.setText("Regular Expression");

        jChkIgnoreCase.setMnemonic('I');
        jChkIgnoreCase.setText("Ignore Case");

        jLabel2.setText("Replace");

        jTglHighlight.setText("Highlight");
        jTglHighlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTglHighlightActionPerformed(evt);
            }
        });

        jCmbReplace.setEditable(true);

        jCmbFind.setEditable(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jChkRegex)
                            .addComponent(jChkIgnoreCase))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(178, 178, 178)
                                .addComponent(jLblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                                .addGap(3, 3, 3))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jChkWrap, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jCmbFind, 0, 337, Short.MAX_VALUE)
                    .addComponent(jCmbReplace, 0, 337, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jBtnReplaceAll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                    .addComponent(jBtnNext, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                    .addComponent(jTglHighlight, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLblStatus))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(jCmbFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jBtnNext))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jBtnReplaceAll)
                            .addComponent(jCmbReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jChkRegex)
                            .addComponent(jChkWrap, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTglHighlight))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jChkIgnoreCase)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNextActionPerformed
        try {
            updateFinder();
            finder.doFindNext(textComponent);
            textComponent.requestFocusInWindow();
        } catch (PatternSyntaxException ex) {
            showRegexpError(ex);
        }
    }//GEN-LAST:event_jBtnNextActionPerformed

    private void jBtnReplaceAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnReplaceAllActionPerformed
        try {
            updateFinder();
            String replacement = (String) jCmbReplace.getSelectedItem();
            ActionUtils.insertIntoCombo(jCmbFind, replacement);
            finder.replaceAll(textComponent, replacement);
            textComponent.requestFocusInWindow();
        } catch (PatternSyntaxException ex) {
            showRegexpError(ex);
        }
}//GEN-LAST:event_jBtnReplaceAllActionPerformed

    private void jTglHighlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTglHighlightActionPerformed
        updateFinder();
        updateHighlights();
    }//GEN-LAST:event_jTglHighlightActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnNext;
    private javax.swing.JButton jBtnReplaceAll;
    private javax.swing.JCheckBox jChkIgnoreCase;
    private javax.swing.JCheckBox jChkRegex;
    private javax.swing.JCheckBox jChkWrap;
    private javax.swing.JComboBox jCmbFind;
    private javax.swing.JComboBox jCmbReplace;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLblStatus;
    private javax.swing.JToggleButton jTglHighlight;
    // End of variables declaration//GEN-END:variables

    public void caretUpdate(CaretEvent e) {
        updateHighlights();
    }

    public void keyTyped(KeyEvent arg0) {
        
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.setVisible(false);
        }
    }

    public void keyReleased(KeyEvent arg0) {
        
    }

    public void componentAdded(ContainerEvent e) {
        registerKeyAction(e.getChild());
    }

    public void componentRemoved(ContainerEvent e) {
        registerKeyAction(e.getChild());
    }

    private void registerKeyAction(Component c) {
        if (c instanceof ReplaceDialog == false) {
            c.removeKeyListener(this);
            c.addKeyListener(this);
        }

        if (c instanceof Container) {
            Container cnt = (Container) c;
            cnt.removeContainerListener(this);
            cnt.addContainerListener(this);
            Component[] ch = cnt.getComponents();
            for (int i = 0; i < ch.length; i++) {
                registerKeyAction(ch[i]);
            }
        }
    }
}
