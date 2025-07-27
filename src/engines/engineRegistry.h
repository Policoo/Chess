#pragma once

#include <array>
#include <memory>

#include "engine.h"
#include "greedy/greedy.h"
#include "random/random.h"    // <— include your concrete engines here

static std::array<std::unique_ptr<Engine>, 2> engines = {
    std::make_unique<Random>(),
    std::make_unique<Greedy>()
};