package me.nuguri.reactivewebfluxchattserver;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MonoAndFluxTest {

    @Test
    void test() {
        final List<String> basket1 = Arrays.asList(new String[]{"kiwi", "orange", "lemon", "orange", "lemon", "kiwi"});
        final List<String> basket2 = Arrays.asList(new String[]{"banana", "lemon", "lemon", "kiwi"});
        final List<String> basket3 = Arrays.asList(new String[]{"strawberry", "orange", "lemon", "grape", "strawberry"});
        final List<List<String>> baskets = Arrays.asList(basket1, basket2, basket3);
        final Flux<List<String>> basketFlux = Flux.fromIterable(baskets);

        basketFlux.concatMap(baskect -> { // List<String>
            Mono<List<String>> distinctFruits = Flux
                    .fromIterable(baskect) // Flux<String>, Iterable 구현체 컬렉션을 Flux<T> 변환
                    .distinct() // Flux<String>, 중복 element 제거
                    .collectList(); // Mono<List<String>>, Flux<T> => Mono<List<T>> 변환
            // 아직 publisher 구독하지 않았음, 데이터 발행 X

            Mono<Map<String, Long>> countFruits = Flux
                    .fromIterable(baskect) // Flux<String>, Iterable 구현체 컬렉션을 Flux<T> 변환
                    .groupBy(fruit -> fruit) // Flux<GroupedFlux<String, String>>, element 그룹핑
                    .concatMap(groupedFlux -> groupedFlux // GroupedFlux<String, String>
                            .count() // Mono<Long>, 그룹핑 Flux element 갯수 집계
                            .map(count -> {
                                final Map<String, Long> fruitCount = new LinkedHashMap<>();
                                String key = groupedFlux.key(); // groupBy 기준 키 값
                                fruitCount.put(key, count); // key => groupBy 기준 키 값, value => groupBy element 갯수
                                return fruitCount; // 즉 각 과일별 갯수를 key, value Map형태로 반환
                            })
                    ) // Flux<Map<String, Long>>, 순서를 보장하는 map 처리, 동기 처리
                    .reduce((mergedMap, map) -> new LinkedHashMap<String, Long>() {
                        { // anonymous class 생성, 괄호를 추가로 적는 경우 생성자를 정의할 수 있음.
                            putAll(mergedMap); // map에 담긴 내용을 map에 모두 담기
                            putAll(map);
                        }
                    });// Mono<Map<String, Long>>, 다수의 내용을 하나의 값(Mono)으로 연산
            // 아직 publisher 구독하지 않았음, 데이터 발행 X

            // Flux<Tuple2<List<String>, Map<String, Long>>> 두 개의 스트림을 합쳐서 하나의 스트림으로 리턴
            // Flux<Tuple2<List<String>, Map<String, Long>>> zip = Flux.zip(distinctFruits, countFruits);

            // Flux<FruitInfo>, 두 개의 스트림을 하나의 스트림으로 변환하고 element를 지정한 객체 타입으로 변경 가능
            return Flux.zip(distinctFruits, countFruits, (distinct, count) -> new FruitInfo(distinct, count));
        }) // Flux<FruitInfo>, Flux<List<String>> => Flux<FruitInfo> concatMap 결과
        .subscribe(System.out::println); // 스트림의 각 element FruitInfo 출력, 구독 및 발행 시작
    }

    @ToString
    static class FruitInfo {

        final List<String> distinctFruits;

        final Map<String, Long> countFruits;

        public FruitInfo(List<String> distinctFruits, Map<String, Long> countFruits) {
            this.distinctFruits = distinctFruits;
            this.countFruits = countFruits;
        }

    }

}
