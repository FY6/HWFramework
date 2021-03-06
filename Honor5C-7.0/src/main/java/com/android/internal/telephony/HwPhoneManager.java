package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.AbstractCallManager.CallManagerReference;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker.CdmaCallTrackerReference;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker.GsmCallTrackerReference;
import com.android.internal.telephony.AbstractGsmCdmaConnection.HwCdmaConnectionReference;
import com.android.internal.telephony.AbstractGsmCdmaPhone.CDMAPhoneReference;
import com.android.internal.telephony.AbstractGsmCdmaPhone.GSMPhoneReference;
import com.android.internal.telephony.AbstractPhoneBase.HwPhoneBaseReference;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker.ImsPhoneCallTrackerReference;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import java.util.Locale;
import java.util.regex.Pattern;

public interface HwPhoneManager {
    public static final String C2 = "F79345FB999D4D9B8A1E1FDA9BFBAAF8";
    public static final int MATCH_GROUP_DIALING_NUMBER = 16;
    public static final int MATCH_GROUP_PWD_CONFIRM = 15;
    public static final int MATCH_GROUP_SIA = 6;
    public static final int MATCH_GROUP_SIB = 9;
    public static final int MATCH_GROUP_SIC = 12;
    public static final Pattern sPatternSuppService = null;

    public interface PhoneServiceInterface {
        void setPhone(Phone phone, Context context);

        void setPhone(Phone[] phoneArr, Context context);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwPhoneManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwPhoneManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwPhoneManager.<clinit>():void");
    }

    void addSpecialAPN(Phone phone);

    void addVirtualNetSpecialFile(String str, String str2, byte[] bArr);

    void addVirtualNetSpecialFile(String str, String str2, byte[] bArr, int i);

    boolean changeMMItoUSSD(GsmCdmaPhone gsmCdmaPhone, String str);

    void changedDefaultTimezone();

    String convertUssdMessage(String str);

    CDMAPhoneReference createHwCDMAPhoneReference(AbstractGsmCdmaPhone abstractGsmCdmaPhone);

    CallManagerReference createHwCallManagerReference(AbstractCallManager abstractCallManager);

    CdmaCallTrackerReference createHwCdmaCallTrackerReference(AbstractGsmCdmaCallTracker abstractGsmCdmaCallTracker);

    HwCdmaConnectionReference createHwCdmaConnectionReference(AbstractGsmCdmaConnection abstractGsmCdmaConnection);

    GSMPhoneReference createHwGSMPhoneReference(AbstractGsmCdmaPhone abstractGsmCdmaPhone);

    GsmCallTrackerReference createHwGsmCallTrackerReference(AbstractGsmCdmaCallTracker abstractGsmCdmaCallTracker);

    ImsPhoneCallTrackerReference createHwImsPhoneCallTrackerReference(AbstractImsPhoneCallTracker abstractImsPhoneCallTracker);

    HwPhoneBaseReference createHwPhoneBaseReference(AbstractPhoneBase abstractPhoneBase);

    <T> T createHwRil(Context context, int i, int i2, Integer num);

    String custCountryCodeForMcc(int i);

    String custLanguageForMcc(int i);

    String custScriptForMcc(int i);

    int custSmallestDigitsMccForMnc(int i);

    String custTimeZoneForMcc(int i);

    String getBetterMatchLocale(Context context, String str, String str2, String str3);

    Locale getBetterMatchLocale(Context context, String str, String str2, String str3, Locale locale);

    String getCDMAVoiceMailNumberHwCust(Context context, String str, int i);

    CharSequence getCallForwardingString(Context context, String str);

    String getPesn(AbstractPhoneSubInfo abstractPhoneSubInfo);

    String getRoamingBrokerImsi();

    String getRoamingBrokerImsi(Integer num);

    String getRoamingBrokerOperatorNumeric();

    String getRoamingBrokerOperatorNumeric(Integer num);

    String getRoamingBrokerVoicemail();

    String getRoamingBrokerVoicemail(int i);

    Locale getSpecialLoacleConfig(Context context, int i);

    String getVirtualNetEccNoCard(int i);

    String getVirtualNetEccWihCard(int i);

    String getVirtualNetNumeric();

    String getVirtualNetOperatorName(String str, boolean z, boolean z2, int i, String str2);

    String getVirtualNetVoiceMailNumber();

    String getVirtualNetVoicemailTag();

    void handleMessageGsmMmiCode(GsmMmiCode gsmMmiCode, Message message);

    int handlePasswordError(String str);

    void initHwTimeZoneUpdater(Context context);

    boolean isDefaultTimezone();

    boolean isRoamingBrokerActivated();

    boolean isRoamingBrokerActivated(Integer num);

    boolean isShortCodeCustomization();

    boolean isStringHuaweiCustCode(String str);

    boolean isStringHuaweiIgnoreCode(GsmCdmaPhone gsmCdmaPhone, String str);

    boolean isSupportEccFormVirtualNet();

    boolean isSupportOrangeApn(Phone phone);

    boolean isVirtualNet();

    void loadHuaweiPhoneService(Phone phone, Context context);

    void loadHuaweiPhoneService(Phone[] phoneArr, Context context);

    void loadVirtualNet(String str, SIMRecords sIMRecords);

    void loadVirtualNetSpecialFiles(String str, SIMRecords sIMRecords);

    CharSequence processBadPinString(Context context, String str);

    boolean processImsPhoneMmiCode(GsmMmiCode gsmMmiCode, Phone phone);

    CharSequence processgoodPinString(Context context, String str);

    void saveUiccCardsToVirtualNet(UiccCard[] uiccCardArr);

    void setDefaultTimezone(Context context);

    void setGsmCdmaPhone(GsmCdmaPhone gsmCdmaPhone, Context context);

    void setMccTableIccId(String str);

    void setMccTableImsi(String str);

    void setMccTableMnc(int i);

    void setRoamingBrokerIccId(String str);

    void setRoamingBrokerIccId(String str, int i);

    void setRoamingBrokerImsi(String str);

    void setRoamingBrokerImsi(String str, Integer num);

    void setRoamingBrokerOperator(String str);

    void setRoamingBrokerOperator(String str, int i);

    boolean shouldSkipUpdateMccMnc(String str);

    int showMmiError(int i);

    int siToServiceClass(String str);

    void triggerLogRadar(String str, String str2);

    String updateSelectionForRoamingBroker(String str);

    String updateSelectionForRoamingBroker(String str, int i);

    boolean useAutoSimLan(Context context);
}
