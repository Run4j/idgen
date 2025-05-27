package uz.maniac4j;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;

@Path("/generate")
public class IdGenerator {
    @ConfigProperty(name = "api.key")
    String apiKey;

    private final File file;

    public IdGenerator() {
        this.file = new File("id.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("100");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize file", e);
        }
    }

//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public Long hello() {
//        return incrementAndGet();
//    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.equals(apiKey)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Unauthorized")
                    .build();
        }

        return Response.ok(incrementAndGet()).build();
    }

    public synchronized long incrementAndGet() {
        long currentValue = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                currentValue = Long.parseLong(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Failed to read number from file", e);
        }

        long newValue = currentValue + 1;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(Long.toString(newValue));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write number to file", e);
        }

        return newValue;
    }

    public synchronized long get() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                return Long.parseLong(line.trim());
            }
            return 0L;
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Failed to read number from file", e);
        }
    }
}
