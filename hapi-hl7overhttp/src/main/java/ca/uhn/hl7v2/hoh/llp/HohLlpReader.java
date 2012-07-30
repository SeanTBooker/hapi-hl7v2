package ca.uhn.hl7v2.hoh.llp;

import java.io.IOException;
import java.io.InputStream;

import ca.uhn.hl7v2.hoh.DecodeException;
import ca.uhn.hl7v2.hoh.AbstractHl7OverHttpDecoder;
import ca.uhn.hl7v2.hoh.Hl7OverHttpRequestDecoder;
import ca.uhn.hl7v2.hoh.Hl7OverHttpResponseDecoder;
import ca.uhn.hl7v2.hoh.NoMessageReceivedException;
import ca.uhn.hl7v2.hoh.auth.IAuthorizationServerCallback;
import ca.uhn.hl7v2.hoh.util.HTTPUtils;
import ca.uhn.hl7v2.hoh.util.ServerRoleEnum;
import ca.uhn.hl7v2.llp.HL7Reader;
import ca.uhn.hl7v2.llp.LLPException;

class HohLlpReader implements HL7Reader {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HohLlpReader.class);

	private InputStream myInputStream;
	private Hl7OverHttpLowerLayerProtocol myProtocol;
	private HohLlpWriter myWriter;

	/**
	 * Constructor
	 */
	public HohLlpReader(Hl7OverHttpLowerLayerProtocol theProtocol) {
		myProtocol = theProtocol;
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() throws IOException {
		myInputStream.close();
	}

	public String getMessage() throws LLPException, IOException {
		AbstractHl7OverHttpDecoder decoder;

		if (myProtocol.getRole() == ServerRoleEnum.CLIENT) {
			decoder = new Hl7OverHttpResponseDecoder();
		} else {
			decoder = new Hl7OverHttpRequestDecoder();
		}

		try {
			decoder.readHeadersAndContentsFromInputStreamAndDecode(myInputStream);
		} catch (DecodeException e) {
			if (myProtocol.getRole() == ServerRoleEnum.CLIENT) {
				throw new LLPException("Failed to process response", e);
			} else {
				ourLog.info("Failed to read contents", e);
				HTTPUtils.write400BadRequest(myWriter.getOutputStream(), e.getMessage());

				myInputStream.close();
				myWriter.getOutputStream().close();

				throw new LLPException("Failed to read message", e);
			}
		} catch (NoMessageReceivedException e) {
			return null;
		}

		if (myProtocol.getRole() == ServerRoleEnum.SERVER) {
			IAuthorizationServerCallback authorizationCallback = myProtocol.getAuthorizationServerCallback();
			if (authorizationCallback != null) {
				boolean auth = authorizationCallback.authorize(decoder.getUri(), decoder.getUsername(), decoder.getPassword());
				if (!auth) {
					HTTPUtils.write401Unauthorized(myWriter.getOutputStream());
					throw new LLPException("Authorization at URI[" + decoder.getUri() + "] failed for user[" + decoder.getUsername() + "]");
				}
			}
		}

		return decoder.getMessage();

	}

	public void setInputStream(InputStream theInputStream) throws IOException {
		myInputStream = theInputStream;
	}

	void setWriter(HohLlpWriter theWriter) {
		myWriter = theWriter;
	}

}