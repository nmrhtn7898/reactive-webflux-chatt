package me.nuguri.reactivewebfluxchattserver;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class MonoAndFluxTest {

    // references
    // https://tech.kakao.com/2018/05/29/reactor-programming/

    // Publisher 스트림 element 변경시 flatMap, flatMapSequential, concatMap 연산 사용 가능
    // flatMap => 비동기 O, 순서 보장 X
    // concatMap => 비동기 X, 순서 보장 O
    // flatMapSequential 비동기 O, 순서 보장 O

    /**
     * distinctFruits 중복 제거 리스트와 각 과일의 개수를 묶은 리스트를 하나의 리스트로 묶어서 각각 출력
     * distinctFruits와 countFruits 모두 Flux.fromIterable(basket)로부터 시작해서 각각 basket을 동기적으로 순회
     * 하나의 for each loop 에서 2가지를 한 번에 해결할 수 있는데 총 2번 basket을 순회하고 특별히 스레드를 지정하지 않아서 동기, 블록킹 방식으로 동작
     * 논 블록킹 라이브러리의 장점을 전혀 살릴 수 없고, 효율성도 떨어짐 단순히 Reactor에서 제공하는 연산자들을 조합한 코드에 불과
     * 기본적으로 스케줄러를 지정하지 않는다면 Flux, Mono는 구독 시 현재 쓰레드에서 동작
     * 로그를 출력해보면 모든 코드가 main 쓰레드 동작
     */
    @Test
    void monoAndFluxOperation() {
        final List<String> basket1 = asList("kiwi", "orange", "lemon", "orange", "lemon", "kiwi");
        final List<String> basket2 = asList("banana", "lemon", "lemon", "kiwi");
        final List<String> basket3 = asList("strawberry", "orange", "lemon", "grape", "strawberry");
        final List<List<String>> baskets = asList(basket1, basket2, basket3);
        final Flux<List<String>> basketFlux = Flux.fromIterable(baskets);

        basketFlux.flatMapSequential(baskect -> { // List<String>
            Mono<List<String>> distinctFruits = Flux
                    .fromIterable(baskect) // Flux<String>, Iterable 구현체 컬렉션을 Flux<T> 변환
                    .log() // 이전 스트림 연산(fromIterable)의 처리 로그 출력, 반환 타입 및 element 변환 처리 없음
                    .distinct() // Flux<String>, 중복 element 제거
                    .collectList(); // Mono<List<String>>, Flux<T> => Mono<List<T>> 변환
            // 아직 publisher 구독하지 않았음, 데이터 발행 X

            Mono<Map<String, Long>> countFruits = Flux
                    .fromIterable(baskect) // Flux<String>, Iterable 구현체 컬렉션을 Flux<T> 변환
                    .log() // 이전 스트림 연산(fromIterable)의 처리 로그 출력, 반환 타입 및 element 변환 처리 없음
                    .groupBy(fruit -> fruit) // Flux<GroupedFlux<String, String>>, element 그룹핑
                    .flatMapSequential(groupedFlux -> groupedFlux // GroupedFlux<String, String>
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
                    }); // Mono<Map<String, Long>>, 다수의 내용을 하나의 값(Mono)으로 연산
            // 아직 publisher 구독하지 않았음, 데이터 발행 X

            // Flux<Tuple2<List<String>, Map<String, Long>>> 두 개의 스트림을 합쳐서 하나의 스트림으로 리턴
            // Flux<Tuple2<List<String>, Map<String, Long>>> zip = Flux.zip(distinctFruits, countFruits);

            // Flux<FruitInfo>, 두 개의 스트림을 하나의 스트림으로 변환하고 element를 지정한 객체 타입으로 변경 가능
            return Flux.zip(distinctFruits, countFruits, (distinct, count) -> new FruitInfo(distinct, count));
        }) // Flux<FruitInfo>, Flux<List<String>> => Flux<FruitInfo> flatMapSequential 결과
        .subscribe(System.out::println); // 스트림의 각 element FruitInfo 출력, 구독 및 발행 시작
    }

    // subscribeOn 해당 스트림을 구독할 때 동작하는 스케줄러를 지정
    // CountDownLatch 병렬 수행 쓰레드가 비 데몬 쓰레드이기 떄문에 메소드 종료시 쓰레드가 남지 않아 결과가 아무것도 출력되지 않음.

    /**
     * Reactor, RxJava 에서 동시성 지원을 위해 Scheduler를 제공한다.
     * distinctFruits, countFruits 스트림을 각각 병렬로 동작하기 위해 Scheduler 적용
     * 로그를 출력해보면 병렬로 쓰레드를 나눠 동작
     * parallel 스케줄러 => 데몬 쓰레드, main 쓰레드 => 비-데몬 쓰레드
     * main 메소드가 끝나 버리면 비-데몬 쓰레드가 남지 않아 데몬 쓰레드가 남아 동작 중이어도 종료된다.
     * 메소드가 종료되도 계속 동작하는 서버 환경이 아닌 현재처럼 테스트 메소드에서는 CountDownLatch 사용이 필요
     * await() 메소드로 main(비-데몬) 쓰레드가 parallel(데몬) 쓰레드의 동작이 끝날 때 까지 대기
     */
    @Test
    void parallelVersion() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2); // 최대 2개의 쓰레드 동시 수행
        final List<String> basket1 = asList("kiwi", "orange", "lemon", "orange", "lemon", "kiwi");
        final List<String> basket2 = asList("banana", "lemon", "lemon", "kiwi");
        final List<String> basket3 = asList("strawberry", "orange", "lemon", "grape", "strawberry");
        final List<List<String>> baskets = asList(basket1, basket2, basket3);
        final Flux<List<String>> basketFlux = Flux.fromIterable(baskets);

        basketFlux.flatMapSequential(baskect -> { // List<String>
            Mono<List<String>> distinctFruits = Flux
                    .fromIterable(baskect) // Flux<String>, Iterable 구현체 컬렉션을 Flux<T> 변환
                    .log() // 이전 스트림 연산(fromIterable)의 처리 로그 출력, 반환 타입 및 element 변환 처리 없음
                    .distinct() // Flux<String>, 중복 element 제거
                    .collectList() // Mono<List<String>>, Flux<T> => Mono<List<T>> 변환
                    .subscribeOn(Schedulers.parallel()); // 구독시 병렬로 수행, 해당 스트림을 수행하면서 아래의 countFruits 스트림을 수행 가능
            // 아직 publisher 구독하지 않았음, 데이터 발행 X

            Mono<Map<String, Long>> countFruits = Flux
                    .fromIterable(baskect) // Flux<String>, Iterable 구현체 컬렉션을 Flux<T> 변환
                    .log() // 이전 스트림 연산(fromIterable)의 처리 로그 출력, 반환 타입 및 element 변환 처리 없음
                    .groupBy(fruit -> fruit) // Flux<GroupedFlux<String, String>>, element 그룹핑
                    .flatMapSequential(groupedFlux -> groupedFlux // GroupedFlux<String, String>
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
                    }) // Mono<Map<String, Long>>, 다수의 내용을 하나의 값(Mono)으로 모아주는 연산
                    .subscribeOn(Schedulers.parallel()); // 구독시 병렬로 수행, 위의 distinctFruits 스트림을 수행하면서 해당 스트림 수행 가능
            // 아직 publisher 구독하지 않았음, 데이터 발행 X

            // Flux<Tuple2<List<String>, Map<String, Long>>> 두 개의 스트림을 합쳐서 하나의 스트림으로 리턴
            // Flux<Tuple2<List<String>, Map<String, Long>>> zip = Flux.zip(distinctFruits, countFruits);

            // Flux<FruitInfo>, 두 개의 스트림을 하나의 스트림으로 변환하고 element를 지정한 객체 타입으로 변경 가능
            return Flux.zip(distinctFruits, countFruits, (distinct, count) -> new FruitInfo(distinct, count));
        }) // Flux<FruitInfo>, Flux<List<String>> => Flux<FruitInfo> flatMapSequential 결과
        .subscribe(
                System.out::println, // consumer, 스트림의 값이 넘어올 때 호출, onNext(T)
                error -> { // errorConsumer, 스트림 처리 중 에러 발생 시 호출, countDown, onError(Throwable)
                    System.err.println(error);
                    countDownLatch.countDown(); // 병렬 쓰레드 종료 알림, main 쓰레드 대기 해제
                },
                () -> { // 스트림 정상 수행 후 종료시, countDown, onComplete()
                    System.out.println("complete");
                    countDownLatch.countDown(); // 병렬 쓰레드 종료 알림, main 쓰레드 대기 해제
                }
        ); // 스트림의 각 element FruitInfo 출력, 구독 및 발행 시작
        countDownLatch.await(2, TimeUnit.SECONDS); // main 쓰레드가 데몬 쓰레드의 동작 종료를 기다려주는 타임아웃 세컨드 설정
    }


    @ToString
    @RequiredArgsConstructor
    static class FruitInfo {

        final List<String> distinctFruits;

        final Map<String, Long> countFruits;

    }

}
