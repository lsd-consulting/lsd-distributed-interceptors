package com.integreety.yatspec.e2e.captor.name;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceNameDeriver {

    private final String appName;

    public String derive()  {
        return appName.replaceAll(" Service", "").replaceAll(" ", "");
    }
}