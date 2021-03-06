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
package jsyntaxpane;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.undo.UndoManager;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A document that supports being highlighted.  The document maintains an
 * internal List of all the Tokens.  The Tokens are updated using
 * a Lexer, passed to it during construction.
 * 
 * @author Ayman Al-Sairafi
 */
public class SyntaxDocument extends PlainDocument {

    Lexer lexer;
    List<Token> tokens;
    UndoManager undo = new CompoundUndoManager();

    public SyntaxDocument(Lexer lexer) {
        super();
        putProperty(PlainDocument.tabSizeAttribute, 4);
        this.lexer = lexer;
        // Listen for undo and redo events
        addUndoableEditListener(new UndoableEditListener() {

            @Override
            public void undoableEditHappened(UndoableEditEvent evt) {
                if (evt.getEdit().isSignificant()) {
                    undo.addEdit(evt.getEdit());
                }
            }
        });
    }

    /**
     * Parse the entire document and return list of tokens that do not already
     * exist in the tokens list.  There may be overlaps, and replacements, 
     * which we will cleanup later.
     * @return list of tokens that do not exist in the tokens field 
     */
    private void parse() {
        // if we have no lexer, then we must have no tokens...
        if (lexer == null) {
            tokens = null;
            return;
        }
        List<Token> toks = new ArrayList<Token>(getLength() / 10);
        long ts = System.nanoTime();
        int len = getLength();
        try {
            Segment seg = new Segment();
            getText(0, getLength(), seg);
            CharArrayReader reader = new CharArrayReader(seg.array, seg.offset, seg.count);
            lexer.yyreset(reader);
            Token token;
            while ((token = lexer.yylex()) != null) {
                toks.add(token);
            }
        } catch (BadLocationException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            // This will not be thrown from the Lexer
            log.log(Level.SEVERE, null, ex);
        } finally {
            if (log.isLoggable(Level.FINEST)) {
                log.finest(String.format("Parsed %d in %d ms, giving %d tokens\n",
                        len, (System.nanoTime() - ts) / 1000000, toks.size()));
            }
            tokens = toks;
        }
    }

    @Override
    protected void fireChangedUpdate(DocumentEvent e) {
        parse();
        super.fireChangedUpdate(e);
    }

    @Override
    protected void fireInsertUpdate(DocumentEvent e) {
        parse();
        super.fireInsertUpdate(e);
    }

    @Override
    protected void fireRemoveUpdate(DocumentEvent e) {
        parse();
        super.fireRemoveUpdate(e);
    }

    @Override
    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        parse();
        super.fireUndoableEditUpdate(e);
    }

    /**
     * Replace the token with the replacement string
     * @param token
     * @param replacement
     */
    public void replaceToken(Token token, String replacement) {
        try {
            replace(token.start, token.length, replacement, null);
        } catch (BadLocationException ex) {
            log.log(Level.WARNING, "unable to replace token: " + token, ex);
        }
    }

    /**
     * This class is used to iterate over tokens between two positions
     * 
     */
    class TokenIterator implements ListIterator<Token> {

        int start;
        int end;
        int ndx = 0;

        @SuppressWarnings("unchecked")
        private TokenIterator(int start, int end) {
            this.start = start;
            this.end = end;
            if (tokens != null && !tokens.isEmpty()) {
                Token token = new Token(TokenType.COMMENT, start, end - start);
                ndx = Collections.binarySearch((List) tokens, token);
                // we will probably not find the exact token...
                if (ndx < 0) {
                    // so, start from one before the token where we should be...
                    // -1 to get the location, and another -1 to go back..
                    ndx = (-ndx - 1 - 1 < 0) ? 0 : (-ndx - 1 - 1);
                    Token t = tokens.get(ndx);
                    // if the prev token does not overlap, then advance one
                    if (t.end() <= start) {
                        ndx++;
                    }

                }
            }
        }

        @Override
        public boolean hasNext() {
            if (tokens == null) {
                return false;
            }
            if (ndx >= tokens.size()) {
                return false;
            }
            Token t = tokens.get(ndx);
            if (t.start >= end) {
                return false;
            }
            return true;
        }

        @Override
        public Token next() {
            return tokens.get(ndx++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            if (tokens == null) {
                return false;
            }
            if (ndx <= 0) {
                return false;
            }
            Token t = tokens.get(ndx);
            if (t.end() <= start) {
                return false;
            }
            return true;
        }

        @Override
        public Token previous() {
            return tokens.get(ndx--);
        }

        @Override
        public int nextIndex() {
            return ndx + 1;
        }

        @Override
        public int previousIndex() {
            return ndx - 1;
        }

        @Override
        public void set(Token e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Token e) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Return an iterator of tokens between p0 and p1.
     * @param start start position for getting tokens
     * @param end position for last token
     * @return Iterator for tokens that overal with range from start to end
     */
    public Iterator<Token> getTokens(int start, int end) {
        return new TokenIterator(start, end);
    }

    /**
     * Find the token at a given position.  May return null if no token is
     * found (whitespace skipped) or if the position is out of range:
     * @param pos
     * @return
     */
    public Token getTokenAt(int pos) {
        if (tokens == null || tokens.isEmpty() || pos > getLength()) {
            return null;
        }
        Token tok = null;
        Token tKey = new Token(TokenType.DEFAULT, pos, 1);
        @SuppressWarnings("unchecked")
        int ndx = Collections.binarySearch((List) tokens, tKey);
        if (ndx < 0) {
            // so, start from one before the token where we should be...
            // -1 to get the location, and another -1 to go back..
            ndx = (-ndx - 1 - 1 < 0) ? 0 : (-ndx - 1 - 1);
            Token t = tokens.get(ndx);
            if ((t.start <= pos) && (pos <= t.end())) {
                tok = t;
            }
        } else {
            tok = tokens.get(ndx);
        }
        return tok;
    }

    /**
     * This is used to return the other part of a paired token in the document.
     * A paired part has token.pairValue <> 0, and the paired token will
     * have the negative of t.pairValue.
     * This method properly handles nestings of same pairValues, but overlaps
     * are not checked.
     * if The document does not contain a paired
     * @param t
     * @return the other pair's token, or null if nothing is found.
     */
    public Token getPairFor(Token t) {
        if (t == null || t.pairValue == 0) {
            return null;
        }
        Token p = null;
        int ndx = tokens.indexOf(t);
        // w will be similar to a stack. The openners weght is added to it
        // and the closers are subtracted from it (closers are already negative)
        int w = t.pairValue;
        int direction = (t.pairValue > 0) ? 1 : -1;
        boolean done = false;
        int v = Math.abs(t.pairValue);
        while (!done) {
            ndx += direction;
            if (ndx < 0 || ndx >= tokens.size()) {
                break;
            }
            Token current = tokens.get(ndx);
            if (Math.abs(current.pairValue) == v) {
                w += current.pairValue;
                if (w == 0) {
                    p = current;
                    done = true;
                }
            }
        }

        return p;
    }

    /**
     * Perform an undo action, if possible
     */
    public void doUndo() {
        if (undo.canUndo()) {
            undo.undo();
            parse();
        }
    }

    /**
     * Perform a redo action, if possible.
     */
    public void doRedo() {
        if (undo.canRedo()) {
            undo.redo();
            parse();
        }
    }

    /**
     * Find the location of the given String in the document.  returns -1
     * if the search string is not found starting at position <code>start</code>
     * @param search The String to search for
     * @param start The beginning index of search
     * @return
     * @deprecated use {@link getMatcher} instead
     */
    @Deprecated
    public int getIndexOf(String search, int start) {
        int flag = Pattern.LITERAL;
        Pattern pattern = Pattern.compile(search, flag);
        return getIndexOf(pattern, start);
    }

    /**
     * Find the next position that matches <code>pattern</code> in the document.
     * returns -1 if the pattern is not found.
     * @param pattern the regex pattern to find
     * @param start The beginning index of search
     * @return
     * @deprecated use {@link getMatcher} instead
     */
    @Deprecated
    public int getIndexOf(Pattern pattern, int start) {
        int ndx = -1;
        if (pattern == null || getLength() == 0) {
            return -1;
        }
        try {
            Segment segment = new Segment();
            getText(start, getLength() - start, segment);
            Matcher m = pattern.matcher(segment);
            if (m.find()) {
                // remember that the index is relative to the document, so
                // always add the start position to it
                ndx = m.start() + start;
            }
        } catch (BadLocationException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return ndx;
    }

    /**
     * Return a matcher that matches the given pattern on the entire document
     * @param pattern
     * @return matcher object
     */
    public Matcher getMatcher(Pattern pattern) {
        return getMatcher(pattern, 0, getLength());
    }

    /**
     * Return a matcher that matches the given pattern in the part of the
     * document starting at offset start.  Note that the matcher will have
     * offset starting from <code>start</code>
     *
     * @param pattern
     * @param start
     * @return matcher that <b>MUST</b> be offset by start to get the proper
     * location within the document
     */
    public Matcher getMatcher(Pattern pattern, int start) {
        return getMatcher(pattern, start, getLength() - start);
    }

    /**
     * Return a matcher that matches the given pattern in the part of the
     * document starting at offset start and ending at start + length.
     * Note that the matcher will have
     * offset starting from <code>start</code>
     *
     * @param pattern
     * @param start
     * @param length
     * @return matcher that <b>MUST</b> be offset by start to get the proper
     * location within the document
     */
    public Matcher getMatcher(Pattern pattern, int start, int length) {
        Matcher matcher = null;
        if (getLength() == 0) {
            return null;
        }
        try {
            Segment seg = new Segment();
            getText(start, length, seg);
            matcher = pattern.matcher(seg);
        } catch (BadLocationException ex) {
            log.log(Level.SEVERE, "Requested offset: " + ex.offsetRequested(), ex);
        }
        return matcher;
    }

    /**
     * This will discard all undoable edits
     */
    public void clearUndos() {
        undo.discardAllEdits();
    }

    /**
     * Gets the line at given position.  The line returned will NOT include
     * the line terminator '\n'
     * @param pos Position (usually from text.getCaretPosition()
     * @return the STring of text at given position
     * @throws BadLocationException
     */
    public String getLineAt(int pos) throws BadLocationException {
        Element e = getParagraphElement(pos);
        Segment seg = new Segment();
        getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset(), seg);
        char last = seg.last();
        if (last == '\n' || last == '\r') {
            return seg.subSequence(0, seg.length() - 1).toString();
        }
        return seg.toString();
    }

    /**
     * Deletes the line at given position
     * @param pos
     * @throws javax.swing.text.BadLocationException
     */
    public void removeLineAt(int pos)
            throws BadLocationException {
        Element e = getParagraphElement(pos);
        remove(e.getStartOffset(), getElementLength(e));
    }

    /**
     * Replace the line at given position with the given string, which can span
     * multiple lines
     * @param pos
     * @param newLines
     * @throws javax.swing.text.BadLocationException
     */
    public void replaceLineAt(int pos, String newLines)
            throws BadLocationException {
        Element e = getParagraphElement(pos);
        replace(e.getStartOffset(), getElementLength(e), newLines, null);
    }

    /**
     * Helper method to get the length of an element and avoid getting
     * a too long element at the end of the document
     * @param e
     * @return
     */
    private int getElementLength(Element e) {
        int end = e.getEndOffset();
        if (end >= (getLength() - 1)) {
            end--;
        }
        return end - e.getStartOffset();
    }

    /**
     * Gets the text without the comments. For example for the string
     * <code>{ // it's a comment</code> this method will return "{ ".
     * @param aStart start of the text.
     * @param anEnd end of the text.
     * @return String for the line without comments (if exists).
     */
    public synchronized String getUncommentedText(int aStart, int anEnd) {
        readLock();
        StringBuilder result = new StringBuilder();
        Iterator<Token> iter = getTokens(aStart, anEnd);
        while (iter.hasNext()) {
            Token t = iter.next();
            if (TokenType.COMMENT != t.type && TokenType.COMMENT2 != t.type) {
                result.append(t.getText(this));
            }
        }
        readUnlock();
        return result.toString();
    }

    /**
     * Returns the starting position of the line at pos
     * @param pos
     * @return starting position of the line
     */
    public int getLineStartOffset(int pos) {
        return getParagraphElement(pos).getStartOffset();
    }

    /**
     * Returns the end position of the line at pos.
     * Does a bounds check to ensure the returned value does not exceed
     * document length
     * @param pos
     * @return
     */
    public int getLineEndOffset(int pos) {
        int end = 0;
        end = getParagraphElement(pos).getEndOffset();
        if (end >= getLength()) {
            end = getLength();
        }
        return end;
    }

    /**
     * Return the number of lines in this document
     * @return
     */
    public int getLineCount() {
        Element e = getDefaultRootElement();
        int cnt = e.getElementCount();
        return cnt;
    }

    /**
     * Return the line number at given position.  The line numbers are zero based
     * @param pos
     * @return
     */
    public int getLineNumberAt(int pos) {
        int lineNr = getDefaultRootElement().getElementIndex(pos);
        return lineNr;
    }

    @Override
    public String toString() {
        return "SyntaxDocument(" + lexer + ", " + ((tokens == null) ? 0 : tokens.size()) + " tokens)@" +
                hashCode();
    }

// our logger instance...
    private static final Logger log = Logger.getLogger(SyntaxDocument.class.getName());
}
