/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.nebula.distribute.lock.core;

import com.nebula.distribute.lock.exception.DistributedLockException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedissonDistributedLockTemplateTest {
    
    @Mock
    private RedissonClient redissonClient;
    
    private RedissonDistributedLockTemplate template;
    
    @BeforeEach
    void setUp() {
        template = new RedissonDistributedLockTemplate(redissonClient);
    }
    
    /** Helper: create a lock mock that holds the lock by default */
    private RLock createLock(String name, RedissonClient client) {
        RLock lock = mock(RLock.class);
        lenient().when(client.getLock(name)).thenReturn(lock);
        lenient().when(client.getFairLock(name)).thenReturn(lock);
        lenient().when(lock.isLocked()).thenReturn(true);
        lenient().when(lock.isHeldByCurrentThread()).thenReturn(true);
        return lock;
    }
    
    private RLock createLockNotHeld(String name, RedissonClient client) {
        RLock lock = mock(RLock.class);
        lenient().when(client.getLock(name)).thenReturn(lock);
        lenient().when(client.getFairLock(name)).thenReturn(lock);
        lenient().when(lock.isLocked()).thenReturn(false);
        return lock;
    }
    
    @Test
    void lockWithWatchDogEnabled() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        String result = template.lock(distributedLock, false, true);
        
        assertEquals("result", result);
        verify(lock).lock();
        verify(lock, never()).lock(anyLong(), eq(TimeUnit.SECONDS));
        verify(lock).unlock();
    }
    
    @Test
    void lockWithWatchDogDisabled() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        String result = template.lock(distributedLock, 20, TimeUnit.SECONDS, false, false);
        
        assertEquals("result", result);
        verify(lock).lock(20L, TimeUnit.SECONDS);
        verify(lock).unlock();
    }
    
    @Test
    void lockDefaultUsesWatchDog() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        String result = template.lock(distributedLock, false, true);
        
        assertEquals("result", result);
        verify(lock).lock();
        verify(lock, never()).lock(anyLong(), eq(TimeUnit.SECONDS));
    }
    
    @Test
    void lockReleasesOnProcessException() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                throw new RuntimeException("process failed");
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        assertThrows(RuntimeException.class, () -> template.lock(distributedLock, 20, TimeUnit.SECONDS, false, false));
        verify(lock).lock(20L, TimeUnit.SECONDS);
        verify(lock).unlock();
    }
    @Test
    void tryLockWithWatchDogEnabled() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        when(lock.tryLock(30L, TimeUnit.SECONDS)).thenReturn(true);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        String result = template.tryLock(distributedLock, 30, 20, TimeUnit.SECONDS, false, true);
        
        assertEquals("result", result);
        verify(lock).tryLock(30L, TimeUnit.SECONDS);
        verify(lock).unlock();
    }
    
    @Test
    void tryLockWithWatchDogDisabled() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        when(lock.tryLock(30L, 20L, TimeUnit.SECONDS)).thenReturn(true);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        String result = template.tryLock(distributedLock, 30, 20, TimeUnit.SECONDS, false, false);
        
        assertEquals("result", result);
        verify(lock).tryLock(30L, 20L, TimeUnit.SECONDS);
        verify(lock).unlock();
    }
    
    @Test
    void tryLockThrowsWhenCannotAcquire() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        when(lock.tryLock(30L, 20L, TimeUnit.SECONDS)).thenReturn(false);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        assertThrows(DistributedLockException.class, () -> template.tryLock(distributedLock, 30, 20, TimeUnit.SECONDS, false, false));
    }
    
    @Test
    void tryLockReleasesOnProcessException() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        when(lock.tryLock(30L, 20L, TimeUnit.SECONDS)).thenReturn(true);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                throw new RuntimeException("process failed");
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        assertThrows(RuntimeException.class, () -> template.tryLock(distributedLock, 30, 20, TimeUnit.SECONDS, false, false));
        verify(lock).unlock();
    }
    
    @Test
    void tryLockReleasesOnInterrupt() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        lenient().when(lock.isLocked()).thenReturn(false);
        lenient().when(lock.isHeldByCurrentThread()).thenReturn(false);
        when(lock.tryLock(30L, 20L, TimeUnit.SECONDS)).thenThrow(new InterruptedException());
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        assertThrows(DistributedLockException.class, () -> template.tryLock(distributedLock, 30, 20, TimeUnit.SECONDS, false, false));
        assertTrue(Thread.currentThread().isInterrupted());
    }
    
    @Test
    void fairLockUsesFairLock() throws Throwable {
        RLock lock = createLock("myFairLock", redissonClient);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myFairLock";
            }
        };
        
        template.lock(distributedLock, 20, TimeUnit.SECONDS, true, false);
        
        verify(redissonClient).getFairLock("myFairLock");
        verify(lock).lock(20L, TimeUnit.SECONDS);
    }
    
    @Test
    void lockNotReleasesWhenNotHeldByCurrentThread() throws Throwable {
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("myLock")).thenReturn(lock);
        when(lock.isHeldByCurrentThread()).thenReturn(false);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        template.lock(distributedLock, 20, TimeUnit.SECONDS, false, false);
        
        verify(lock, never()).unlock();
    }
    
    @Test
    void lockNotReleasesWhenNotLocked() throws Throwable {
        RLock lock = createLockNotHeld("myLock", redissonClient);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        template.lock(distributedLock, 20, TimeUnit.SECONDS, false, false);
        
        verify(lock, never()).unlock();
    }
    
    @Test
    void defaultTryLockUsesDefaultValues() throws Throwable {
        RLock lock = createLock("myLock", redissonClient);
        when(lock.tryLock(30L, TimeUnit.SECONDS)).thenReturn(true);
        
        DistributedLock<String> distributedLock = new DistributedLock<>() {
            
            @Override
            public String process() {
                return "result";
            }
            
            @Override
            public String lockName() {
                return "myLock";
            }
        };
        
        template.tryLock(distributedLock, false, true);
        
        verify(lock).tryLock(30L, TimeUnit.SECONDS);
    }
}
