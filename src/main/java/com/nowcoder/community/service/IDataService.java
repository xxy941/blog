package com.nowcoder.community.service;

import java.util.Date;

public interface IDataService {

    public void recordUV(String ip);

    public long calculateUV(Date start, Date end);

    public void recordDAU(int userId);

    public long calculateDAU(Date start,Date end);
}
