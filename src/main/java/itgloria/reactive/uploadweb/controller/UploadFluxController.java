package itgloria.reactive.uploadweb.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
public class UploadFluxController {

  // use Flux<FilePart> for multiple file upload
  @PostMapping(
    value = "/upload-flux",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.APPLICATION_NDJSON_VALUE
  )
  public Flux<String> upload(
    @RequestPart("files") Flux<FilePart> filePartFlux
  ) {
    return getLines(filePartFlux);
  }

  //   filePartFlux will emit filepart into the flatmap. Then we access the content of filepart and map to create a Flux of String. Inside the map, we get dataBuffer , which is emitted from the content(). Here we have to keep in mind that a certain amount of bytes are readable from this dataBuffer. So, we take a byte array variable bytes with a length of dataBuffer.readableByteCount()
  // Then we fill the bytes array by reading data from dataBuffer like dataBuffer.read(bytes) . Then we free the dataBuffer by releasing it like DataBufferUtils.release(dataBuffer) . Then we convert the bytes into String and return it. So, when this full process will be completed we will get a new Flux<String> stream. Now let's see the rest of the method.

  public Flux<String> getLines(Flux<FilePart> filePartFlux) {
    return filePartFlux.flatMap(
      filePart ->
        filePart
          .content()
          .map(
            dataBuffer -> {
              byte[] bytes = new byte[dataBuffer.readableByteCount()];
              dataBuffer.read(bytes);
              DataBufferUtils.release(dataBuffer);

              String a = new String(bytes, StandardCharsets.UTF_8);
              return a;
            }
          )
          .map(this::processAndGetLinesAsList)
          .flatMapIterable(Function.identity())
    );
  }

  // check againsts the regular pattern
  private List<String> processAndGetLinesAsList(String string) {
    Supplier<Stream<String>> streamSupplier = string::lines;
    var isFileOk = streamSupplier
      .get()
      .anyMatch(line -> line.contains("gloria"));

    log.info("fileok?" + isFileOk);

    return isFileOk
      ? streamSupplier.get().collect(Collectors.toList())
      : new ArrayList<>();
  }
}
