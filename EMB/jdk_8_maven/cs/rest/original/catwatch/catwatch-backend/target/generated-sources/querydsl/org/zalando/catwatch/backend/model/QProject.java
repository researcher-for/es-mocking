package org.zalando.catwatch.backend.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QProject is a Querydsl query type for Project
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QProject extends EntityPathBase<Project> {

    private static final long serialVersionUID = -896694496L;

    public static final QProject project = new QProject("project");

    public final NumberPath<Integer> commitsCount = createNumber("commitsCount", Integer.class);

    public final NumberPath<Integer> contributorsCount = createNumber("contributorsCount", Integer.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> externalContributorsCount = createNumber("externalContributorsCount", Integer.class);

    public final NumberPath<Integer> forksCount = createNumber("forksCount", Integer.class);

    public final NumberPath<Long> gitHubProjectId = createNumber("gitHubProjectId", Long.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath image = createString("image");

    public final ListPath<String, StringPath> languageList = this.<String, StringPath>createList("languageList", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath lastPushed = createString("lastPushed");

    public final ListPath<String, StringPath> maintainers = this.<String, StringPath>createList("maintainers", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final StringPath organizationName = createString("organizationName");

    public final StringPath primaryLanguage = createString("primaryLanguage");

    public final NumberPath<Integer> score = createNumber("score", Integer.class);

    public final DateTimePath<java.util.Date> snapshotDate = createDateTime("snapshotDate", java.util.Date.class);

    public final NumberPath<Integer> starsCount = createNumber("starsCount", Integer.class);

    public final StringPath title = createString("title");

    public final StringPath url = createString("url");

    public QProject(String variable) {
        super(Project.class, forVariable(variable));
    }

    public QProject(Path<? extends Project> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProject(PathMetadata<?> metadata) {
        super(Project.class, metadata);
    }

}

