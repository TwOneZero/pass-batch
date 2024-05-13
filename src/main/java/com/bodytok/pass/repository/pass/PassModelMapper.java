package com.bodytok.pass.repository.pass;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PassModelMapper {

    PassModelMapper INSTANCE = Mappers.getMapper(PassModelMapper.class);

    //필드명이 같은 건 그대로, 같지 않은 건 명시해서 매핑해줌
    @Mapping(target = "status", qualifiedByName = "status")
    @Mapping(target = "remainingCount", source = "bulkPassEntity.count")
    PassEntity toPassEntity(BulkPassEntity bulkPassEntity, String userId);

    @Named("status")
    default PassStatus status(BulkPassStatus status) {
        return PassStatus.READY;
    }
}
