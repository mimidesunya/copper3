package jp.cssj.sakae.gc.text.layout;

import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.text.CharacterHandler;
import jp.cssj.sakae.gc.text.Quad;

public class FilterCharacterHandler implements CharacterHandler {
	protected CharacterHandler characterHandler;

	public FilterCharacterHandler(CharacterHandler characterHandler) {
		this.setCharacterHandler(characterHandler);
	}

	public FilterCharacterHandler() {
		// default
	}

	public CharacterHandler getCharacterHandler() {
		return this.characterHandler;
	}

	public void setCharacterHandler(CharacterHandler characterHandler) {
		this.characterHandler = characterHandler;
	}

	public void characters(int charOffset, char[] ch, int off, int len) {
		this.characterHandler.characters(charOffset, ch, off, len);
	}

	public void flush() {
		this.characterHandler.flush();
	}

	public void finish() {
		this.characterHandler.flush();
	}

	public void quad(Quad quad) {
		this.characterHandler.quad(quad);
	}

	public void fontStyle(FontStyle fontStyle) {
		this.characterHandler.fontStyle(fontStyle);
	}

}
