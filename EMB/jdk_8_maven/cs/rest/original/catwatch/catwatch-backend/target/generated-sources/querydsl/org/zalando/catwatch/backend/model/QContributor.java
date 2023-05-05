package org.zalando.catwatch.backend.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QContributor is a Querydsl query type for Contributor
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QContributor extends EntityPathBase<Contributor> {

    private static final long serialVersionUID = -211140254L;

    public static final QContributor contributor = new QContributor("contributor");

    public final SimplePath<ContributorKey> key = createSimple("key", ContributorKey.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> organizationalCommitsCount = createNumber("organizationalCommitsCount", Integer.class);

    public final NumberPath<Integer> organizationalProjectsCount = createNumber("organizationalProjectsCount", Integer.class);

    public final StringPath organizationName = createString("organizationName");

    public final NumberPath<Integer> personalCommitsCount = createNumber("personalCommitsCount", Integer.class);

    public final NumberPath<Integer> personalProjectsCount = createNumber("personalProjectsCount", Integer.class);

    public final StringPath url = createString("url");

    public QContributor(String variable) {
        super(Contributor.class, forVariable(variable));
    }

    public QContributor(Path<? extends Contributor> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContributor(PathMetadata<?> metadata) {
        super(Contributor.class, metadata);
    }

}

