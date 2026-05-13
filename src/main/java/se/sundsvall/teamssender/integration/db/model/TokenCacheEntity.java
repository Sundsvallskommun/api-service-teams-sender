package se.sundsvall.teamssender.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
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

	public static TokenCacheEntity create() {
		return new TokenCacheEntity();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public TokenCacheEntity withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public byte[] getCacheData() {
		return cacheData;
	}

	public void setCacheData(final byte[] cacheData) {
		this.cacheData = cacheData;
	}

	public TokenCacheEntity withCacheData(final byte[] cacheData) {
		this.cacheData = cacheData;
		return this;
	}

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(final Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public TokenCacheEntity withLastUpdated(final Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final TokenCacheEntity that = (TokenCacheEntity) o;
		return Objects.equals(userId, that.userId) && Arrays.equals(cacheData, that.cacheData) && Objects.equals(lastUpdated, that.lastUpdated);
	}

	@Override
	public int hashCode() {
		return 31 * Objects.hash(userId, lastUpdated) + Arrays.hashCode(cacheData);
	}

	@Override
	public String toString() {
		return "TokenCacheEntity{" +
			"userId='" + userId + '\'' +
			", cacheData=" + Arrays.toString(cacheData) +
			", lastUpdated=" + lastUpdated +
			'}';
	}
}
