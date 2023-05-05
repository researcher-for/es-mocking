package org.zalando.catwatch.backend.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QStatistics is a Querydsl query type for Statistics
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QStatistics extends EntityPathBase<Statistics> {

    private static final long serialVersionUID = -1148640324L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStatistics statistics = new QStatistics("statistics");

    public final NumberPath<Integer> allContributorsCount = createNumber("allContributorsCount", Integer.class);

    public final NumberPath<Integer> allForksCount = createNumber("allForksCount", Integer.class);

    public final NumberPath<Integer> allSizeCount = createNumber("allSizeCount", Integer.class);

    public final NumberPath<Integer> allStarsCount = createNumber("allStarsCount", Integer.class);

    public final NumberPath<Integer> externalContributorsCount = createNumber("externalContributorsCount", Integer.class);

    public final QStatisticsKey key;

    public final NumberPath<Integer> membersCount = createNumber("membersCount", Integer.class);

    public final StringPath organizationName = createString("organizationName");

    public final NumberPath<Integer> privateProjectCount = createNumber("privateProjectCount", Integer.class);

    public final NumberPath<Integer> programLanguagesCount = createNumber("programLanguagesCount", Integer.class);

    public final NumberPath<Integer> publicProjectCount = createNumber("publicProjectCount", Integer.class);

    public final NumberPath<Integer> tagsCount = createNumber("tagsCount", Integer.class);

    public final NumberPath<Integer> teamsCount = createNumber("teamsCount", Integer.class);

    public QStatistics(String variable) {
        this(Statistics.class, forVariable(variable), INITS);
    }

    public QStatistics(Path<? extends Statistics> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QStatistics(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QStatistics(PathMetadata<?> metadata, PathInits inits) {
        this(Statistics.class, metadata, inits);
    }

    public QStatistics(Class<? extends Statistics> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.key = inits.isInitialized("key") ? new QStatisticsKey(forProperty("key")) : null;
    }

}

