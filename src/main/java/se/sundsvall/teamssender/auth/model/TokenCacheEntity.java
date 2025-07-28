package se.sundsvall.teamssender.auth.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "token_cache")
@Getter
@Setter
public class TokenCacheEntity {

	@Id
	@Column(name = "user_id", nullable = false, length = 255)
	private String userId;
	@Lob
	@Column(name = "cache_data", nullable = false, columnDefinition = "LONGBLOB")
	private byte[] cacheData;

	@UpdateTimestamp
	@Column(name = "last_updated", nullable = false)
	private Timestamp lastUpdated;

	public TokenCacheEntity() {}

	public TokenCacheEntity(String userId, byte[] cacheData) {
		this.userId = userId;
		this.cacheData = cacheData;
	}
}
