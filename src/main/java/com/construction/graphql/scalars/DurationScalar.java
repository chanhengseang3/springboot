package com.construction.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.*;

import java.time.Duration;

import static graphql.scalars.util.Kit.typeName;

public class DurationScalar extends GraphQLScalarType {

   public DurationScalar(){
       super("Duration", "Time Duration, ex:PT2H20M10S", new Coercing() {
           @Override
           public String serialize(Object input) throws CoercingSerializeException {
               if(input instanceof Duration){
                   return ((Duration) input).toString();
               }else if(input instanceof String){
                   return (String) input;
               }
               throw new CoercingSerializeException("Expected a 'String' or 'Duration' but was '" + typeName(input) + "'.");
           }

           @Override
           public Duration parseValue(Object input) throws CoercingParseValueException {
               if(input instanceof Duration){
                   return (Duration)input;
               }else if(input instanceof Long){
                   return Duration.ofSeconds((Long)input);
               }else if(input instanceof String){
                   return Duration.parse((String)input);
               }
               throw new CoercingSerializeException("Expected a 'String' or 'Duration' but was '" + typeName(input) + "'.");
           }

           @Override
           public Duration parseLiteral(Object input) throws CoercingParseLiteralException {
               if(input instanceof StringValue){
                   return Duration.parse(((StringValue)input).getValue());
               }else {
                   throw new CoercingParseLiteralException("Expected a 'String' or 'Duration' but was '" + typeName(input) + "'.");
               }
           }
       });

   }
}
