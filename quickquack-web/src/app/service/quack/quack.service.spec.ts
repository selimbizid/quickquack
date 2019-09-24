import { TestBed, inject } from '@angular/core/testing';

import { QuackService } from './quack.service';

describe('QuackService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [QuackService]
    });
  });

  it('should ...', inject([QuackService], (service: QuackService) => {
    expect(service).toBeTruthy();
  }));
});
