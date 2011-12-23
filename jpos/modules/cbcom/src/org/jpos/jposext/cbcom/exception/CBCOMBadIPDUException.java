package org.jpos.jposext.cbcom.exception;

public class CBCOMBadIPDUException extends CBCOMException {

	public enum ReasonEnum {
		INVALID_PGI(0x02), INVALID_LGI(0x02), LGI_INCOHERENCE(0x02), LGI_LI_INCOHERENCE(0x26), PREMATURATE_END_OF_DATA(0x27), INVALID_PI(0x04), UNEXPECTED_PI_FOR_IPDU(0x04), PI_REQUIRED_BY_IPDU(0x25), APDU_LEN_TOO_HIGH(0x13);
		
		/**
		 * PI01 value associated<BR>
		 * Typical use is to pass it in an IPDU AB<BR>
		 */		
		private byte pv01;

		private ReasonEnum(int pv01) {
			this.pv01 = (byte) (pv01 & 0xFF);
		}

		public byte getPv01() {
			return pv01;
		}
		
	}

	private ReasonEnum reasonEnum;

	public CBCOMBadIPDUException(ReasonEnum reasonEnum) {
		super(reasonEnum.getPv01());
		this.reasonEnum = reasonEnum;
	}

	public CBCOMBadIPDUException(ReasonEnum reasonEnum, String msg) {
		super(msg, reasonEnum.getPv01());
		this.reasonEnum = reasonEnum;
	}

	public ReasonEnum getReasonEnum() {
		return reasonEnum;
	}

}
