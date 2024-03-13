package jp.cssj.homare.ua.props;

import java.util.Map;

import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.message.MessageHandler;
import jp.cssj.homare.ua.UserAgent;

public class IntegerPropManager extends AbstractPropManager {
	public final int defaultInt;

	public IntegerPropManager(String name, int defaultInt) {
		super(name);
		this.defaultInt = defaultInt;
	}

	public String getDefaultString() {
		return String.valueOf(this.defaultInt);
	}

	public int getInteger(UserAgent ua) {
		return this.getInteger(ua.getProperty(this.name), ua);
	}

	public int getInteger(Map<String, String> props, MessageHandler mh) {
		return this.getInteger((String) props.get(this.name), mh);
	}

	public int getInteger(String str, MessageHandler mh) {
		if (str == null) {
			return this.defaultInt;
		}
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			mh.message(MessageCodes.WARN_BAD_IO_PROPERTY, new String[] { this.name, str });
		}
		return this.defaultInt;
	}
}
