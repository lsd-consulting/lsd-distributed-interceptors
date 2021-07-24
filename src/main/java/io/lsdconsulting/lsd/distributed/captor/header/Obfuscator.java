package io.lsdconsulting.lsd.distributed.captor.header;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Obfuscator {

    public Map<String, Collection<String>> obfuscate(Map<String, Collection<String>> headers) {
        if (headers.containsKey("Authorization")) {
            headers = new HashMap<>(headers);
            headers.put("Authorization", List.of("<obfuscated>"));
        }
        return headers;
    }
}
