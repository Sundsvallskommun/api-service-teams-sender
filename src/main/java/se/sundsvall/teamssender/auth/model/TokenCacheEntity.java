package se.sundsvall.teamssender.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "token_cache")
public class TokenCacheEntity {

	@Id
	@Column(name = "user_id", nullable = false)
	private String userId;

	@Lob
	@Column(name = "cache_data", nullable = false, columnDefinition = "LONGBLOB")
	private byte[] cacheData;

	@UpdateTimestamp
	@Column(name = "last_updated", nullable = false)
	private Timestamp lastUpdated;

	public TokenCacheEntity() {}

	public TokenCacheEntity(final String userId, final byte[] cacheData) {
		this.userId = userId;
		this.cacheData = cacheData;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public byte[] getCacheData() {
		return cacheData;
	}

	public void setCacheData(final byte[] cacheData) {
		this.cacheData = cacheData;
	}

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(final Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
