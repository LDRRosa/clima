package com.grpc.clima.servidor;

import com.grpc.clima.*; 
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService 
public class WeatherServiceImpl extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final Map<String, List<Float>> database = new ConcurrentHashMap<>();

    public WeatherServiceImpl() {
        // Dados iniciais de exemplo
        database.put("Urutai", new ArrayList<>(Arrays.asList(25.5f, 26.0f, 24.0f, 22.5f, 28.0f)));
    }

    @Override
    public void obterTemperaturaAtual(CidadeRequest request, StreamObserver<TemperaturaResponse> responseObserver) {
        List<Float> temps = database.get(request.getNome());
        float temp = (temps != null) ? temps.get(temps.size() - 1) : 0.0f;

        responseObserver.onNext(TemperaturaResponse.newBuilder()
                .setNome(request.getNome())
                .setTemperatura(temp)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void listarCidades(Empty request, StreamObserver<ListaCidadesResponse> responseObserver) {
        responseObserver.onNext(ListaCidadesResponse.newBuilder()
                .addAllNomes(database.keySet())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void cadastrarCidade(NovaCidadeRequest request, StreamObserver<StatusResponse> responseObserver) {
        database.put(request.getNome(), new ArrayList<>(Collections.singletonList(request.getTemperaturaInicial())));

        responseObserver.onNext(StatusResponse.newBuilder()
                .setMensagem("Cidade " + request.getNome() + " cadastrada!")
                .setSucesso(true)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void previsaoCincoDias(CidadeRequest request, StreamObserver<PrevisaoResponse> responseObserver) {
        List<Float> temps = database.get(request.getNome());
        List<Float> previsao = new ArrayList<>();

        if (temps != null && !temps.isEmpty()) {
            float baseTemp = temps.get(temps.size() - 1);
            previsao.add(baseTemp);
            Random random = new Random();
            for (int i = 0; i < 4; i++) {
                previsao.add(baseTemp + (random.nextFloat() * 4 - 2)); 
            }
        } else {
            for (int i = 0; i < 5; i++)
                previsao.add(0.0f);
        }

        responseObserver.onNext(PrevisaoResponse.newBuilder()
                .addAllTemperaturas(previsao) 
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void estatisticasClimaticas(CidadeRequest request, StreamObserver<EstatisticasResponse> responseObserver) {
        List<Float> temps = database.getOrDefault(request.getNome(), Collections.emptyList());

        float min = temps.stream().min(Float::compare).orElse(0.0f);
        float max = temps.stream().max(Float::compare).orElse(0.0f);
        double media = temps.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);

        responseObserver.onNext(EstatisticasResponse.newBuilder()
                .setMinima(min)
                .setMaxima(max)
                .setMedia((float) media)
                .build());
        responseObserver.onCompleted();
    }

}