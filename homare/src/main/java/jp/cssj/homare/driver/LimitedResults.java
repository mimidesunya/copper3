package jp.cssj.homare.driver;

import java.io.IOException;

import jp.cssj.cti2.CTISession;
import jp.cssj.cti2.results.Results;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.ua.AbortException;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.resolver.MetaSource;
import jp.cssj.rsr.RandomBuilder;
import jp.cssj.rsr.helpers.RandomBuilderWrapper;

public class LimitedResults implements Results {
	private final Results results;
	private final long limit;
	private final UserAgent ua;
	private long traffic = 0;

	public LimitedResults(final Results results, final long limit, final UserAgent ua) {
		this.results = results;
		this.limit = limit;
		this.ua = ua;
	}

	public long getTraffic() {
		return this.traffic;
	}

	public boolean hasNext() {
		return this.results.hasNext();
	}

	public RandomBuilder nextBuilder(final MetaSource metaSource) throws IOException {
		final RandomBuilder builder = this.results.nextBuilder(metaSource);
		return new RandomBuilderWrapper(builder) {
			public void write(int id, byte[] b, int off, int len) throws IOException {
				traffic += len;
				if (traffic > limit) {
					short code = MessageCodes.ERROR_OUTPUT_FILE_TOO_LARGE;
					String[] args = new String[] { String.valueOf(limit) };
					ua.message(code, args);
					throw new AbortException(CTISession.ABORT_FORCE);
				}
				super.write(id, b, off, len);
			}
		};
	}

	public void end() throws IOException {
		this.results.end();
	}
}
