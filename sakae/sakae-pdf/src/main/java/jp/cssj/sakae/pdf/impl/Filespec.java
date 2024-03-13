package jp.cssj.sakae.pdf.impl;

import jp.cssj.sakae.pdf.Attachment;
import jp.cssj.sakae.pdf.ObjectRef;

class Filespec {
	final Attachment attachment;

	final String name;

	final ObjectRef ref;

	Filespec(Attachment attachment, String name, ObjectRef ref) {
		this.attachment = attachment;
		this.name = name;
		this.ref = ref;
	}
}
