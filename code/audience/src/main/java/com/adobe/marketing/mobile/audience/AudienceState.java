/* ***********************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package com.adobe.marketing.mobile.audience;

import com.adobe.marketing.mobile.LocalStorageService.DataStore;

import java.util.HashMap;
import java.util.Map;

/**
 * AudienceState class is responsible for the following:
 * <ol>
 *     <li>Keeping the current state of all Audience-related variables.</li>
 *     <li>Persisting variables via the {@link LocalStorageService}.</li>
 *     <li>Providing getters and setters for all maintained variables.</li>
 * </ol>
 */
class AudienceState {
	private static final String LOG_TAG = AudienceState.class.getSimpleName();

	// configuration settings
	private String uuid = null;
	private String dpid = null;
	private String dpuuid = null;
	private Map<String, String> visitorProfile = null;
	private LocalStorageService localStorageService;
	private MobilePrivacyStatus privacyStatus = AudienceConstants.DEFAULT_PRIVACY_STATUS;

	/**
	 * Constructor.
	 *
	 * @param storageService {@link LocalStorageService} instance
	 */
	AudienceState(final LocalStorageService storageService) {
		this.localStorageService = storageService;
	}

	// ========================================================
	// package-protected methods
	// ========================================================
	/**
	 * Sets the value of this {@link #dpid} property.
	 * <p>
	 * Setting the identifier is ignored if the global privacy is set to {@link MobilePrivacyStatus#OPT_OUT}.
	 *
	 * @param dpid {@link String} containing the new value for {@code dpid}
	 */
	void setDpid(final String dpid) {
		if (StringUtils.isNullOrEmpty(dpid) || privacyStatus != MobilePrivacyStatus.OPT_OUT) {
			this.dpid = dpid;
		}
	}

	/**
	 * Sets the value of this {@link #dpuuid} property.
	 * <p>
	 * Setting the identifier is ignored if the global privacy is set to {@link MobilePrivacyStatus#OPT_OUT}.
	 *
	 * @param dpuuid {@link String} containing the new value for {@code dpuuid}
	 */
	void setDpuuid(final String dpuuid) {
		if (StringUtils.isNullOrEmpty(dpuuid) || privacyStatus != MobilePrivacyStatus.OPT_OUT) {
			this.dpuuid = dpuuid;
		}
	}

	/**
	 * Sets the value of this {@link #uuid} property.
	 * <p>
	 * Persists the new value to the {@link LocalStorageService.DataStore}.
	 * <p>
	 * Setting the identifier is ignored if the global privacy is set to {@link MobilePrivacyStatus#OPT_OUT}.
	 *
	 * @param uuid {@link String} containing the new value for {@code uuid}
	 */
	void setUuid(final String uuid) {
		// update uuid in data store

		DataStore audienceDataStore = getDataStore();

		if (audienceDataStore != null) {
			if (StringUtils.isNullOrEmpty(uuid)) {
				audienceDataStore.remove(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_USER_ID_KEY);
			} else if (privacyStatus != MobilePrivacyStatus.OPT_OUT) {
				audienceDataStore.setString(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_USER_ID_KEY, uuid);
			}
		} else {
			Log.error(LOG_TAG, "setUuid - Unable to update uuid in persistence as LocalStorage service was not initialized");
		}

		if (StringUtils.isNullOrEmpty(uuid) || privacyStatus != MobilePrivacyStatus.OPT_OUT) {
			this.uuid = uuid;
		}
	}

	/**
	 * Sets the value of this {@link #visitorProfile} property.
	 * <p>
	 * Persists the new value to the {@link LocalStorageService.DataStore}.
	 * <p>
	 * Setting the identifier is ignored if the global privacy is set to {@link MobilePrivacyStatus#OPT_OUT}.
	 *
	 * @param visitorProfile {@code Map<String, String>} containing the new {@code visitorProfile}
	 */
	void setVisitorProfile(final Map<String, String> visitorProfile) {
		// update the visitor profile in our data store
		DataStore audienceDataStore = getDataStore();

		if (audienceDataStore != null) {
			if (visitorProfile == null || visitorProfile.isEmpty()) {
				audienceDataStore.remove(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_PROFILE_KEY);
			} else if (privacyStatus != MobilePrivacyStatus.OPT_OUT) {
				audienceDataStore.setMap(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_PROFILE_KEY, visitorProfile);
			}
		} else {
			Log.error(LOG_TAG,
					  "setVisitorProfile - Unable to update visitor profile in persistence as LocalStorage service was not initialized");
		}

		if (visitorProfile == null || visitorProfile.isEmpty() || privacyStatus != MobilePrivacyStatus.OPT_OUT) {
			this.visitorProfile = visitorProfile;
		}
	}

	/**
	 * Sets the {@code MobilePrivacyStatus} for this {@code AudienceState}.
	 * @param privacyStatus the {@link MobilePrivacyStatus} to set for this {@link AudienceState}
	 */
	void setMobilePrivacyStatus(final MobilePrivacyStatus privacyStatus) {
		this.privacyStatus = privacyStatus;
	}

	/**
	 * Returns this {@link #dpid}.
	 *
	 * @return {@link String} containing {@code dpid} value
	 */
	String getDpid() {
		return dpid;
	}

	/**
	 * Returns this {@link #dpuuid}.
	 *
	 * @return {@link String} containing {@code dpuuid} value
	 */
	String getDpuuid() {
		return dpuuid;
	}

	/**
	 * Returns this {@link #uuid}.
	 * <p>
	 * If there is no {@code uuid} value in memory, this method attempts to find one from the {@link LocalStorageService.DataStore}.
	 *
	 * @return {@link String} containing {@code uuid} value
	 */
	String getUuid() {
		if (StringUtils.isNullOrEmpty(uuid)) {
			// load uuid from data store if we have one
			DataStore audienceDataStore = getDataStore();

			if (audienceDataStore != null) {
				uuid = audienceDataStore.getString(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_USER_ID_KEY, uuid);
			} else {
				Log.error(LOG_TAG, "getUuid - Unable to retrieve uuid from persistence as LocalStorage service was not initialized");
			}
		}

		return uuid;
	}

	/**
	 * Returns this {@link #visitorProfile}.
	 * <p>
	 * If there is no {@code visitorProfile} value in memory, this method attempts to find one from the {@link LocalStorageService.DataStore}.
	 *
	 * @return {@code Map<String, String>} containing visitor profile
	 */
	Map<String, String> getVisitorProfile() {
		if (visitorProfile == null || visitorProfile.isEmpty()) {
			// load visitor profile from data store if we have one
			DataStore audienceDataStore = getDataStore();

			if (audienceDataStore == null) {
				Log.error(LOG_TAG,
						  "getVisitorProfile - Unable to retrieve visitor profile from persistence as LocalStorage service was not initialized");
			} else if (audienceDataStore.contains(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_PROFILE_KEY)) {
				visitorProfile = audienceDataStore.getMap(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_PROFILE_KEY);
			}
		}

		return visitorProfile;
	}

	/**
	 * Gets the {@code MobilePrivacyStatus} for this {@code AudienceState}.
	 * @return the {@link MobilePrivacyStatus} for this {@link AudienceState}
	 */
	MobilePrivacyStatus getMobilePrivacyStatus() {
		return privacyStatus;
	}

	/**
	 * Get the data for this {@code AudienceState} instance to share with other modules.
	 * The state data is only populated if the set privacy status is not {@link MobilePrivacyStatus#OPT_OUT}.
	 *
	 * @return {@link EventData} map of this {@link AudienceState}
	 */
	EventData getStateData() {
		final EventData stateData = new EventData();

		if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
			// do not share state if privacy is Opt-Out
			return stateData;
		}

		String dpid = getDpid();

		if (!StringUtils.isNullOrEmpty(dpid)) {
			stateData.putString(AudienceConstants.EventDataKeys.Audience.DPID, dpid);
		}

		String dpuuid = getDpuuid();

		if (!StringUtils.isNullOrEmpty(dpuuid)) {
			stateData.putString(AudienceConstants.EventDataKeys.Audience.DPUUID, dpuuid);
		}

		String uuid = getUuid();

		if (!StringUtils.isNullOrEmpty(uuid)) {
			stateData.putString(AudienceConstants.EventDataKeys.Audience.UUID, uuid);
		}

		Map<String, String> profile = getVisitorProfile();

		if (profile != null) {
			stateData.putStringMap(AudienceConstants.EventDataKeys.Audience.VISITOR_PROFILE, profile);
		}

		return stateData;
	}

	/**
	 * Clear the identifiers for this {@code AudienceState}.
	 * The cleared identifiers are:
	 * <ul>
	 *     <li>UUID</li>
	 *     <li>DPID</li>
	 *     <li>DPUUID</li>
	 *     <li>Visitor Profiles</li>
	 * </ul>
	 */
	void clearIdentifiers() {
		setUuid(null);
		setDpid(null);
		setDpuuid(null);
		setVisitorProfile(null);
	}

	// ========================================================
	// private methods
	// ========================================================
	/**
	 * Returns {@code DataStore} from this {@link #localStorageService}.
	 *
	 * @return {@code DataStore} for the {@code Audience} module or null if {@link LocalStorageService} is unavailable
	 */
	private DataStore getDataStore() {
		if (localStorageService ==  null) {
			return null;
		}

		return localStorageService.getDataStore(AudienceConstants.AUDIENCE_MANAGER_SHARED_PREFS_DATA_STORE);
	}
}
